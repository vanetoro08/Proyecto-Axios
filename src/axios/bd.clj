(ns axios.bd
  "Acceso a la base de datos SQLite (contratos.md, sección Base de datos).
  Responsable: Vanessa (PP-29 a PP-33).

  Todo el léxico, las reglas, las señales y el historial viven en la BD.
  Nada de eso queda escrito en el código: sale de resources/bd/*.sql y de
  los archivos que se cargan con la orden `cargar`.

  Se usa SQLite con JDBC estándar (java.sql), sin librerías extra (D-001)."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.sql Connection DriverManager ResultSet)))

;; ---------------------------------------------------------------------------
;; Conexión
;; ---------------------------------------------------------------------------

(def ^:dynamic *ruta-bd*
  "Archivo de la BD, en la carpeta desde donde se ejecuta el programa.
  Las pruebas lo cambian con binding para trabajar en una BD aparte."
  "axios.db")

(defn- abrir ^Connection []
  (let [c (DriverManager/getConnection (str "jdbc:sqlite:" *ruta-bd*))]
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

(defonce ^:private inicializadas (atom #{}))

(defn- conexion
  "Abre una conexión a la BD, inicializándola la primera vez."
  ^Connection []
  (when-not (contains? @inicializadas *ruta-bd*)
    (locking inicializadas
      (when-not (contains? @inicializadas *ruta-bd*)
        (inicializar!)
        (swap! inicializadas conj *ruta-bd*))))
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

(defn- preparar [^Connection c sql params]
  (let [ps (.prepareStatement c sql)]
    (doseq [[i p] (map-indexed vector params)]
      (.setObject ps (int (inc i)) p))
    ps))

(defn- consultar-en
  "SELECT dentro de una conexión abierta (para usarlo en una transacción)."
  [^Connection c sql & params]
  (with-open [ps (preparar c sql params)
              rs (.executeQuery ps)]
    (leer-filas rs)))

(defn- ejecutar-en!
  "INSERT o UPDATE dentro de una conexión abierta. Devuelve el id de la fila
  insertada (en un UPDATE no se usa)."
  [^Connection c sql & params]
  (with-open [ps (preparar c sql params)]
    (.executeUpdate ps))
  (:id (first (consultar-en c "SELECT last_insert_rowid() AS id"))))

(defn- en-transaccion
  "Ejecuta (f conexion) en una sola transacción: o entra todo o no entra nada."
  [f]
  (with-open [c (conexion)]
    (.setAutoCommit c false)
    (try
      (let [r (f c)]
        (.commit c)
        r)
      (catch Exception e
        (.rollback c)
        (throw e))
      (finally
        (.setAutoCommit c true)))))

(defn- consultar
  "Ejecuta un SELECT con parámetros (?) y devuelve una lista de mapas."
  [sql & params]
  (with-open [c (conexion)]
    (apply consultar-en c sql params)))

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
                                  WHERE p.lengua = 'es' AND p.forma = ? AND p.vigente_hasta IS NULL
                                  ORDER BY p.id"
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

(defn verbos-sin-objeto
  "Infinitivos de los verbos que no llevan objeto (D-020): #{\"venir\" \"llegar\" \"estar\"}."
  []
  (into #{} (map :infinitivo) (consultar "SELECT infinitivo FROM verbo_sin_objeto")))

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
;; Historial de preguntas (PP-32)
;; Cada pregunta se guarda completa: con sus correcciones, sus dos traducciones
;; y qué palabra de la BD se usó en cada traducción (traduccion_palabra). Así se
;; puede demostrar que nada se inventó y qué se creía cuando se envió.
;; ---------------------------------------------------------------------------

(defn- edn [x] (when (some? x) (pr-str x)))

(defn- leer-edn [s] (when s (edn/read-string s)))

(defn- id-de-palabra
  "La fila de palabra que respalda una palabra generada: la que trae el
  generador (:palabra-id) o, si no la trae, la vigente de su concepto."
  [c lengua {:keys [palabra-id concepto]}]
  (or palabra-id
      (when concepto
        (:id (first (consultar-en c "SELECT p.id FROM palabra p JOIN concepto co ON co.id = p.concepto_id
                                     WHERE co.clave = ? AND p.lengua = ? AND p.vigente_hasta IS NULL
                                     ORDER BY p.id"
                                  (name concepto) (name lengua)))))))

(defn registrar-pregunta!
  "Guarda el resultado completo del pipeline en la BD, en una sola transacción.
  Se llama siempre, también cuando la pregunta se rechaza (condición 1).
  Devuelve el id de la pregunta."
  [{:keys [fecha original normalizado arbol estado explicacion avisos correcciones traducciones]}]
  (en-transaccion
   (fn [c]
     (let [id (ejecutar-en! c "INSERT INTO pregunta (fecha, original, normalizado, arbol, estado, explicacion, avisos)
                               VALUES (?, ?, ?, ?, ?, ?, ?)"
                            fecha original (:texto normalizado) (edn arbol)
                            (name (or estado :rechazada)) explicacion (edn (vec avisos)))]
       (doseq [{:keys [original corregida distancia pos]} correcciones]
         (ejecutar-en! c "INSERT INTO correccion (pregunta_id, original, corregida, distancia, pos_inicio, pos_fin)
                          VALUES (?, ?, ?, ?, ?, ?)"
                       id original corregida distancia (:inicio pos) (:fin pos)))
       (doseq [[lengua {:keys [texto valida? errores palabras]}] traducciones]
         (let [tid (ejecutar-en! c "INSERT INTO traduccion (pregunta_id, lengua, texto, valida, errores)
                                    VALUES (?, ?, ?, ?, ?)"
                                 id (name lengua) texto (if valida? 1 0) (edn errores))]
           (doseq [[orden p] (map-indexed vector palabras)]
             (when-let [pid (id-de-palabra c lengua p)]
               (ejecutar-en! c "INSERT INTO traduccion_palabra (traduccion_id, palabra_id, orden)
                                VALUES (?, ?, ?)"
                             tid pid orden)))))
       id))))

(defn- traducciones-de [c pregunta-id]
  (into {}
        (for [t (consultar-en c "SELECT id, lengua, texto, valida, errores FROM traduccion
                                 WHERE pregunta_id = ? ORDER BY id" pregunta-id)]
          [(keyword (:lengua t))
           {:texto    (:texto t)
            :valida?  (= 1 (:valida t))
            :errores  (or (leer-edn (:errores t)) [])
            :palabras (vec (consultar-en c "SELECT p.forma, co.clave AS concepto, p.fuente_id
                                            FROM traduccion_palabra tp
                                            JOIN palabra p ON p.id = tp.palabra_id
                                            JOIN concepto co ON co.id = p.concepto_id
                                            WHERE tp.traduccion_id = ? ORDER BY tp.orden"
                                         (:id t)))}])))

(defn historial
  "Preguntas registradas, de la más antigua a la más reciente. Con fecha
  (\"2026-11-21\"), solo las que se hicieron desde ese día."
  ([] (historial nil))
  ([desde]
   (with-open [c (conexion)]
     (vec
      (for [p (if desde
                (consultar-en c "SELECT * FROM pregunta WHERE fecha >= ? ORDER BY id" desde)
                (consultar-en c "SELECT * FROM pregunta ORDER BY id"))]
        {:id           (:id p)
         :fecha        (:fecha p)
         :original     (:original p)
         :normalizado  (:normalizado p)
         :arbol        (leer-edn (:arbol p))
         :estado       (keyword (:estado p))
         :explicacion  (:explicacion p)
         :avisos       (or (leer-edn (:avisos p)) [])
         :correcciones (vec (consultar-en c "SELECT original, corregida, distancia FROM correccion
                                             WHERE pregunta_id = ? ORDER BY id" (:id p)))
         :traducciones (traducciones-de c (:id p))})))))

;; ---------------------------------------------------------------------------
;; Orden `cargar` (PP-30, bitácora D-002)
;; ---------------------------------------------------------------------------

(defn- hoy
  "Fecha de hoy como texto ISO, ej. \"2026-11-24\"."
  []
  (str (java.time.LocalDate/now)))

(defn- partir-campos
  "Parte una línea en sus campos separados por |. Se recorre carácter por
  carácter porque fuera del normalizador no se usa regex (ni str/split)."
  [linea]
  (loop [cs (seq linea), actual [], campos []]
    (if-let [c (first cs)]
      (if (= c \|)
        (recur (rest cs) [] (conj campos (str/trim (apply str actual))))
        (recur (rest cs) (conj actual c) campos))
      (conj campos (str/trim (apply str actual))))))

(defn- sin-tildes
  "Minúsculas, sin tildes y ñ → n, como guarda el vocabulario (D-003)."
  [texto]
  (->> (java.text.Normalizer/normalize (str/lower-case texto) java.text.Normalizer$Form/NFD)
       (remove #(= (Character/getType ^char %) Character/NON_SPACING_MARK))
       (apply str)))

(defn- version-del-manual
  "Lee la versión del encabezado: \"# senales_semana2.txt · Axios · manual versión 0.8\" → \"0.8\"."
  [lineas]
  (some (fn [l]
          (when (str/starts-with? l "#")
            (when-let [i (str/index-of l "versión")]
              (let [v (str/trim (subs l (+ i (count "versión"))))]
                (when-not (str/blank? v) v)))))
        lineas))

(defn- fuente-de-carga!
  "Crea (una sola vez por carga) la fuente que respalda lo que entra del archivo."
  [c fuentes tipo descripcion fecha]
  (or (get @fuentes tipo)
      (let [id (ejecutar-en! c "INSERT INTO fuente (tipo, descripcion, fecha) VALUES (?, ?, ?)"
                             tipo descripcion fecha)]
        (swap! fuentes assoc tipo id)
        id)))

(defn- cargar-senal!
  "SENAL|dia|voz|transcripcion|salida → tabla senal. Si ya estaba, no la repite."
  [c fuente-id [_ dia voz transcripcion salida]]
  (let [dia (parse-long dia)
        voz (str/lower-case voz)
        transcripcion (when-not (str/blank? transcripcion) transcripcion)]
    (cond
      (nil? dia)
      {:error "el día no es un número"}

      (not (#{"kethari" "ozh" "desconocida"} voz))
      {:error (str "voz desconocida '" voz "' (se espera kethari, ozh o desconocida)")}

      (str/blank? salida)
      {:error "falta la salida del decodificador"}

      (seq (consultar-en c "SELECT id FROM senal
                            WHERE dia = ? AND voz = ? AND transcripcion IS ? AND salida = ?"
                         dia voz transcripcion salida))
      {:repetida true}

      :else
      (do (ejecutar-en! c "INSERT INTO senal (dia, voz, transcripcion, salida, fuente_id)
                           VALUES (?, ?, ?, ?, ?)"
                        dia voz transcripcion salida (fuente-id))
          {:senal true}))))

(defn- concepto-de-significado
  "El concepto al que corresponde un significado en español (\"territorio
  reclamado\" → su fila de concepto), o nil."
  [c significado]
  (first (consultar-en c "SELECT co.id, co.clave, co.categoria
                          FROM palabra p JOIN concepto co ON co.id = p.concepto_id
                          WHERE p.lengua = 'es' AND p.forma = ? AND p.vigente_hasta IS NULL
                          ORDER BY p.id"
                       (sin-tildes significado))))

(defn- crear-concepto!
  "Significado nuevo que la BD no conocía: se crea el concepto (con la misma
  categoría que el significado anterior) y su palabra en español."
  [c significado categoria fuente-id fecha]
  (let [forma (sin-tildes significado)
        clave (str/replace forma " " "-")
        id    (ejecutar-en! c "INSERT INTO concepto (clave, categoria) VALUES (?, ?)" clave categoria)]
    (ejecutar-en! c "INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde)
                     VALUES (?, 'es', ?, ?, ?)"
                  id forma fuente-id fecha)
    {:id id :clave clave :categoria categoria}))

(defn- cargar-correccion!
  "CORRECCION|lengua|palabra|anterior|nuevo: cierra la vigencia del significado
  anterior con la fecha de carga y crea el nuevo desde esa misma fecha."
  [c fuente-id fecha [_ lengua forma anterior nuevo]]
  (let [lengua (str/lower-case lengua)
        concepto-anterior (concepto-de-significado c anterior)
        concepto-nuevo    (concepto-de-significado c nuevo)
        vigente (fn [concepto]
                  (when concepto
                    (first (consultar-en c "SELECT id FROM palabra
                                            WHERE lengua = ? AND forma = ? AND concepto_id = ?
                                              AND vigente_hasta IS NULL"
                                         lengua forma (:id concepto)))))]
    (cond
      (not (#{"kethari" "ozh"} lengua))
      {:error (str "lengua desconocida '" lengua "' (se espera kethari u ozh)")}

      (some str/blank? [forma anterior nuevo])
      {:error "faltan datos: se espera CORRECCION|lengua|palabra|significado anterior|significado nuevo"}

      (vigente concepto-nuevo)
      {:repetida true}

      (nil? (vigente concepto-anterior))
      {:error (str "'" forma "' no significa '" anterior "' en " lengua " según la BD")}

      :else
      (let [fid (fuente-id)
            nuevo (or concepto-nuevo
                      (crear-concepto! c nuevo (:categoria concepto-anterior) fid fecha))]
        (ejecutar-en! c "UPDATE palabra SET vigente_hasta = ? WHERE id = ?"
                      fecha (:id (vigente concepto-anterior)))
        (ejecutar-en! c "INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde)
                         VALUES (?, ?, ?, ?, ?)"
                      (:id nuevo) lengua forma fid fecha)
        {:correccion true}))))

(defn cargar-archivo!
  "Incorpora un archivo de señales o de correcciones del manual (D-002), sin
  tocar el código. Cada línea dice su tipo en el primer campo:
    SENAL|dia|voz|transcripcion|salida
    CORRECCION|lengua|palabra|significado anterior|significado nuevo
  Las líneas con # son comentarios. Una línea que no se entiende se reporta y
  la carga sigue con las demás. Lo que ya estaba cargado no se repite.
  Todo entra en una sola transacción y con la fecha de carga (por defecto, hoy).

  Devuelve {:archivo :version :senales :correcciones :repetidas :errores}."
  ([ruta] (cargar-archivo! ruta (hoy)))
  ([ruta fecha]
   (let [archivo (io/file ruta)]
     (when-not (.isFile archivo)
       (throw (ex-info (str "No existe el archivo '" ruta "'.")
                       {:fase :cargar :tipo :archivo-no-encontrado :detalle {:ruta ruta}})))
     (let [lineas  (with-open [r (io/reader archivo :encoding "UTF-8")] (vec (line-seq r)))
           version (version-del-manual lineas)
           nombre  (str (.getName archivo) (when version (str " (manual " version ")")))
           fuentes (atom {})]
       (en-transaccion
        (fn [c]
          (let [fuente-senal (fn [] (fuente-de-carga! c fuentes "senal"
                                                      (str "Señales de " nombre) fecha))
                fuente-corr  (fn [] (fuente-de-carga! c fuentes "correccion"
                                                      (str "Correcciones de " nombre) fecha))
                resultados
                (for [[n linea] (map-indexed (fn [i l] [(inc i) (str/trim l)]) lineas)
                      :when (not (or (str/blank? linea) (str/starts-with? linea "#")))]
                  (let [campos (partir-campos linea)
                        tipo   (str/upper-case (first campos))
                        r (cond
                            (not (#{"SENAL" "CORRECCION"} tipo))
                            {:error (str "tipo desconocido '" (first campos) "'")}

                            (not= 5 (count campos))
                            {:error (str "se esperaban 5 campos y hay " (count campos))}

                            (= "SENAL" tipo)
                            (cargar-senal! c fuente-senal campos)

                            :else
                            (cargar-correccion! c fuente-corr fecha campos))]
                    (assoc r :linea n)))
                resultados (doall resultados)]
            {:archivo      ruta
             :version      version
             :senales      (count (filter :senal resultados))
             :correcciones (count (filter :correccion resultados))
             :repetidas    (count (filter :repetida resultados))
             :errores      (vec (for [r resultados :when (:error r)]
                                  {:linea (:linea r) :motivo (:error r)}))})))))))

;; ---------------------------------------------------------------------------
;; Orden `palabra` (PP-33)
;; ---------------------------------------------------------------------------

(defn- fecha-valida
  "La fecha tiene que venir como AAAA-MM-DD (ej. 2026-11-21)."
  [fecha]
  (try (str (java.time.LocalDate/parse fecha))
       (catch java.time.format.DateTimeParseException _
         (throw (ex-info (str "La fecha '" fecha "' no es válida. Use el formato AAAA-MM-DD, por ejemplo 2026-11-21.")
                         {:fase :palabra :tipo :fecha-invalida :detalle {:fecha fecha}})))))

(def ^:private vigente-en
  "Condición de vigencia: desde ≤ fecha y (sin hasta o fecha < hasta)."
  "p.vigente_desde <= ? AND (p.vigente_hasta IS NULL OR ? < p.vigente_hasta)")

(defn- version [fila]
  {:forma  (:forma fila)
   :desde  (:vigente-desde fila)
   :hasta  (:vigente-hasta fila)
   :fuente (:fuente fila)})

(defn palabra-info
  "Lo que el sistema sabe de un término, en las tres lenguas.
  El término puede estar en español, Kethari u Ozh (\"nave\", \"kum\", \"gor\").
  Sin fecha (nil): todas las versiones, con su vigencia y su fuente.
  Con fecha (\"2026-11-21\"): solo lo que estaba vigente ese día.

  Devuelve {:termino :fecha :significados [{:lengua :significado :concepto
  :desde :hasta :fuente :equivalentes {:es [...] :kethari [...] :ozh [...]}}]}."
  [termino fecha]
  (let [fecha   (when fecha (fecha-valida fecha))
        termino (str/trim (str/lower-case termino))
        filtro  (if fecha (str " AND " vigente-en) "")
        con-fecha (fn [& params] (if fecha (concat params [fecha fecha]) params))]
    (with-open [c (conexion)]
      (let [filas (apply consultar-en c
                         (str "SELECT p.id, p.lengua, p.forma, p.concepto_id, co.clave,
                                      p.vigente_desde, p.vigente_hasta, f.descripcion AS fuente
                               FROM palabra p
                               JOIN concepto co ON co.id = p.concepto_id
                               JOIN fuente f ON f.id = p.fuente_id
                               WHERE p.forma IN (?, ?)" filtro
                              " ORDER BY p.vigente_desde, p.id")
                         (con-fecha termino (sin-tildes termino)))
            significado (fn [concepto-id]
                          (:forma (first (consultar-en c "SELECT forma FROM palabra
                                                          WHERE concepto_id = ? AND lengua = 'es'
                                                          ORDER BY id" concepto-id))))
            equivalentes (fn [{:keys [concepto-id id]}]
                           (let [otras (apply consultar-en c
                                              (str "SELECT p.lengua, p.forma, p.vigente_desde, p.vigente_hasta,
                                                           f.descripcion AS fuente
                                                    FROM palabra p JOIN fuente f ON f.id = p.fuente_id
                                                    WHERE p.concepto_id = ? AND p.id <> ?" filtro
                                                   " ORDER BY p.id")
                                              (con-fecha concepto-id id))]
                             (into {}
                                   (for [lengua ["es" "kethari" "ozh"]
                                         :let [vs (mapv version (filter #(= lengua (:lengua %)) otras))]
                                         :when (seq vs)]
                                     [(keyword lengua) vs]))))]
        {:termino      termino
         :fecha        fecha
         :significados (vec (for [f filas]
                              (merge {:lengua      (keyword (:lengua f))
                                      :significado (significado (:concepto-id f))
                                      :concepto    (keyword (:clave f))}
                                     (dissoc (version f) :forma)
                                     {:equivalentes (equivalentes f)})))}))))
