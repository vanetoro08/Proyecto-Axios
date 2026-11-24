(ns axios.bd
  "Acceso a la base de datos SQLite (contratos.md, sección Base de datos).
  Responsable: Vanessa (PP-29 a PP-33).

  Todo el léxico, las reglas, las señales y el historial viven en la BD.
  Nada de eso queda escrito en el código: sale de resources/bd/*.sql y de
  los archivos que se cargan con la orden `cargar`.

  Se usa SQLite con JDBC estándar (java.sql), sin librerías extra (D-001)."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.sql Connection DriverManager ResultSet)))

;; ---------------------------------------------------------------------------
;; Conexión
;; ---------------------------------------------------------------------------

(def ^:private ruta-bd
  "Archivo de la BD, en la carpeta desde donde se ejecuta el programa."
  "axios.db")

(defn- abrir ^Connection []
  (let [c (DriverManager/getConnection (str "jdbc:sqlite:" ruta-bd))]
    (with-open [s (.createStatement c)]
      (.execute s "PRAGMA foreign_keys = ON"))
    c))

;; ---------------------------------------------------------------------------
;; Scripts SQL
;; ---------------------------------------------------------------------------

(defn separar-sentencias
  "Parte un script SQL en sentencias, cortando en cada `;` que no esté dentro
  de un texto entre comillas simples. Ignora los comentarios `--`.
  Se recorre carácter por carácter porque fuera del normalizador no se puede
  usar regex (ni str/split con patrón)."
  [texto]
  (loop [cs texto, actual [], en-texto? false, sentencias []]
    (if-let [c (first cs)]
      (cond
        (and (not en-texto?) (= c \-) (= (second cs) \-))
        (recur (drop-while #(not= % \newline) cs) actual false sentencias)

        (= c \')
        (recur (rest cs) (conj actual c) (not en-texto?) sentencias)

        (and (= c \;) (not en-texto?))
        (recur (rest cs) [] false (conj sentencias (str/trim (apply str actual))))

        :else
        (recur (rest cs) (conj actual c) en-texto? sentencias))
      (->> (conj sentencias (str/trim (apply str actual)))
           (remove str/blank?)
           vec))))

(defn- ejecutar-script!
  "Ejecuta todas las sentencias de un archivo de resources/ en una sola
  transacción: o entra completo o no entra nada."
  [^Connection c recurso]
  (let [texto (slurp (io/resource recurso) :encoding "UTF-8")]
    (.setAutoCommit c false)
    (try
      (with-open [s (.createStatement c)]
        (doseq [sentencia (separar-sentencias texto)]
          (.executeUpdate s sentencia)))
      (.commit c)
      (catch Exception e
        (.rollback c)
        (throw e))
      (finally
        (.setAutoCommit c true)))))

(defn- bd-vacia? [^Connection c]
  (with-open [s  (.createStatement c)
              rs (.executeQuery s "SELECT COUNT(*) FROM concepto")]
    (.next rs)
    (zero? (.getInt rs 1))))

(defn- inicializar!
  "Crea las tablas si no existen y, si la BD está vacía, carga el vocabulario y el manual v0.7."
  []
  (with-open [c (abrir)]
    (ejecutar-script! c "bd/esquema.sql")
    (when (bd-vacia? c)
      (ejecutar-script! c "bd/manual_v07.sql")
      (ejecutar-script! c "bd/morfologia.sql")
      (ejecutar-script! c "bd/vocabulario_es.sql"))))

(defonce ^:private lista (delay (inicializar!)))

(defn- conexion
  "Abre una conexión a la BD, inicializándola la primera vez."
  ^Connection []
  @lista
  (abrir))

;; ---------------------------------------------------------------------------
;; Consultas
;; ---------------------------------------------------------------------------

(defn- columna->clave
  "pos_inicio → :pos-inicio"
  [nombre]
  (keyword (str/replace nombre "_" "-")))

(defn- leer-filas [^ResultSet rs]
  (let [md    (.getMetaData rs)
        n     (.getColumnCount md)
        claves (mapv #(columna->clave (.getColumnLabel md (int (inc %)))) (range n))]
    (loop [filas []]
      (if (.next rs)
        (recur (conj filas (zipmap claves (map #(.getObject rs (int (inc %))) (range n)))))
        filas))))

(defn- consultar
  "Ejecuta un SELECT con parámetros (?) y devuelve una lista de mapas."
  [sql & params]
  (with-open [c  (conexion)
              ps (.prepareStatement c sql)]
    (doseq [[i p] (map-indexed vector params)]
      (.setObject ps (int (inc i)) p))
    (with-open [rs (.executeQuery ps)]
      (leer-filas rs))))

;; ---------------------------------------------------------------------------
;; Funciones del contrato
;; ---------------------------------------------------------------------------

(defn vocabulario-es
  "Conjunto de palabras españolas que conoce el sistema (para el corrector)."
  []
  (into #{}
        (map :forma)
        (consultar "SELECT forma FROM palabra WHERE lengua = 'es' AND vigente_hasta IS NULL
                    UNION
                    SELECT forma FROM verbo_irregular")))

(defn buscar-es
  "Categoría y concepto de una palabra española, o nil.
  Ej: (buscar-es \"nave\") → {:concepto :nave :categoria :sustantivo}"
  [forma]
  (when-let [f (first (consultar "SELECT c.clave, c.categoria
                                  FROM palabra p JOIN concepto c ON c.id = p.concepto_id
                                  WHERE p.lengua = 'es' AND p.forma = ? AND p.vigente_hasta IS NULL"
                                 forma))]
    {:concepto (keyword (:clave f)) :categoria (keyword (:categoria f))}))

(defn verbo-irregular
  "Análisis de una forma verbal irregular, o nil.
  La tabla verbo_irregular se llena con los datos que defina Luisa (PP-16)."
  [forma]
  (when-let [f (first (consultar "SELECT infinitivo, tiempo, persona, numero
                                  FROM verbo_irregular WHERE forma = ?"
                                 forma))]
    (let [concepto (:concepto (buscar-es (:infinitivo f)))]
      {:infinitivo (:infinitivo f)
       :concepto   concepto
       :persona    (:persona f)
       :numero     (keyword (:numero f))
       :tiempo     (keyword (:tiempo f))})))

(defn terminaciones-es
  "Reglas de terminación verbal del español.
  La tabla morfologia_es se llena con los datos que defina Luisa (PP-16)."
  []
  (mapv #(-> %
             (update :tiempo keyword)
             (update :numero keyword))
        (consultar "SELECT terminacion, conjugacion, tiempo, persona, numero FROM morfologia_es")))

(defn palabra-en
  "Forma vigente de un concepto en una lengua: {:forma \"kum\" :fuente-id 1 :palabra-id 37} o nil.
  Si hay varias formas (nosotros → anul y anir), devuelve la primera; el generador
  escoge entre ellas con las reglas nosotros.* (D-004)."
  [concepto lengua]
  (when-let [f (first (consultar "SELECT p.id AS palabra_id, p.forma, p.fuente_id
                                  FROM palabra p JOIN concepto c ON c.id = p.concepto_id
                                  WHERE c.clave = ? AND p.lengua = ? AND p.vigente_hasta IS NULL
                                  ORDER BY p.id"
                                 (name concepto) (name lengua)))]
    (select-keys f [:forma :fuente-id :palabra-id])))

(defn reglas
  "Mapa de reglas vigentes de una lengua (:kethari u :ozh).
  Ej: (:sufijo.pasado (reglas :kethari)) → \"ok\""
  [lengua]
  (into {}
        (map (juxt (comp keyword :clave) :valor))
        (consultar "SELECT clave, valor FROM regla WHERE lengua = ? AND vigente_hasta IS NULL"
                   (name lengua))))

(defn formas
  "Conjunto de palabras vigentes de una lengua (para los validadores)."
  [lengua]
  (into #{}
        (map :forma)
        (consultar "SELECT forma FROM palabra WHERE lengua = ? AND vigente_hasta IS NULL"
                   (name lengua))))

;; ---------------------------------------------------------------------------
;; STUB EN MEMORIA — todavía no escriben en la BD.
;; registrar-pregunta! (con traduccion_palabra): PP-32. historial: PP-34.
;; palabra-info: PP-33. cargar-archivo!: PP-30.
;; ---------------------------------------------------------------------------

(defonce ^:private historial* (atom []))

(defn registrar-pregunta!
  "Guarda el resultado completo del pipeline. Devuelve su id."
  [resultado]
  (let [id (inc (count @historial*))]
    (swap! historial* conj (assoc resultado :id id))
    id))

(defn historial
  "Preguntas registradas, opcionalmente desde una fecha (texto ISO)."
  ([] (historial nil))
  ([desde]
   (if desde
     (filter #(>= (compare (:fecha %) desde) 0) @historial*)
     @historial*)))

(defn palabra-info
  "Lo que el sistema sabe de un término en las tres lenguas.
  Sin fecha (nil): todas las versiones, con su vigencia y su fuente.
  Con fecha: solo la versión vigente en esa fecha. TODO PP-33 / PP-34."
  [termino _fecha]
  {:termino termino :significados []})

(defn cargar-archivo!
  "Incorpora un archivo de señales o de correcciones del manual. TODO PP-30."
  [ruta]
  {:archivo ruta :senales 0 :palabras-nuevas 0})