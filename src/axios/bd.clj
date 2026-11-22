(ns axios.bd
  "Acceso a la base de datos SQLite (contratos.md, sección 10).
  Responsable: Vanessa (PP-29 a PP-33).

  Todo el léxico, las reglas, las señales y el historial viven en la BD.
  Nada de eso puede quedar escrito en el código de la entrega final.")

;; ---------------------------------------------------------------------------
;; STUB EN MEMORIA — mismos nombres de funciones que tendrá la versión real.
;; Los datos de ejemplo de abajo SOLO existen para que los demás puedan
;; trabajar mientras se hace PP-29. Se borran cuando la BD real esté lista
;; (si quedan en la entrega, incumplimos "nada del léxico en el código").
;; ---------------------------------------------------------------------------

(defonce ^:private historial* (atom []))

(defn vocabulario-es
  "Conjunto de palabras españolas que conoce el sistema (para el corrector)."
  []
  #{"cuando" "vendran" "ustedes"})

(defn buscar-es
  "Categoría y concepto de una palabra española, o nil."
  [forma]
  (get {"cuando"  {:categoria :interrogativo :concepto :cuando}
        "ustedes" {:categoria :pronombre :concepto :ustedes}}
       forma))

(defn verbo-irregular
  "Análisis de una forma verbal irregular, o nil."
  [forma]
  (get {"vendran" {:infinitivo "venir" :concepto :venir
                   :persona 3 :numero :plural :tiempo :futuro}}
       forma))

(defn terminaciones-es
  "Reglas de terminación verbal del español."
  []
  [])

(defn palabra-en
  "Forma de un concepto en una lengua: {:forma \"kum\" :fuente-id 1} o nil."
  [_concepto _lengua]
  nil)

(defn reglas
  "Mapa de reglas gramaticales vigentes de una lengua (:kethari u :ozh)."
  [_lengua]
  {})

(defn formas
  "Conjunto de palabras válidas de una lengua (para los validadores)."
  [_lengua]
  #{})

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
