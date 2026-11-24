(ns axios.morfologia
  "Análisis morfológico del español (PP-16).
  Contrato: (analizar tokens) → tokens con :categoria, :concepto y, según la
  categoría, :verbo o :pronombre (contratos.md, sección 4).
  Responsable: Luisa (E1).
  Las palabras, terminaciones y verbos irregulares salen de la BD; aquí solo
  está el procedimiento."
  (:require [axios.bd :as bd]
            [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Pronombres: persona y número del concepto neutral del árbol intermedio
;; (contratos.md, sección 6). No es léxico: son las claves del árbol.
;; ---------------------------------------------------------------------------

(def ^:private persona-de-pronombre
  {:yo       {:persona 1 :numero :singular}
   :tu       {:persona 2 :numero :singular}
   :usted    {:persona 2 :numero :singular}
   :el       {:persona 3 :numero :singular}
   :el-pron  {:persona 3 :numero :singular}
   :ella     {:persona 3 :numero :singular}
   :nosotros {:persona 1 :numero :plural}
   :ustedes  {:persona 2 :numero :plural}
   :ellos    {:persona 3 :numero :plural}})

;; ---------------------------------------------------------------------------
;; Verbos
;; ---------------------------------------------------------------------------

(def ^:private orden-tiempo {:presente 0 :pasado 1 :futuro 2})

(defn- terminaciones-ordenadas
  "La terminación más larga primero (\"llegaremos\" es -aremos, no -emos).
  Con el mismo largo, primero el presente (D-015)."
  []
  (sort-by (fn [{:keys [terminacion tiempo]}]
             [(- (count terminacion)) (orden-tiempo (keyword tiempo) 9)])
           (bd/terminaciones-es)))

(defn- verbo-en-bd
  "El infinitivo solo vale si la BD lo tiene como verbo. Así no se inventan verbos."
  [infinitivo]
  (let [info (bd/buscar-es infinitivo)]
    (when (= :verbo (:categoria info)) info)))

(defn- por-terminacion [lexema terminaciones]
  (some (fn [{:keys [terminacion conjugacion tiempo persona numero]}]
          (when (and (str/ends-with? lexema terminacion)
                     (> (count lexema) (count terminacion)))
            (let [infinitivo (str (subs lexema 0 (- (count lexema) (count terminacion))) conjugacion)]
              (when-let [v (verbo-en-bd infinitivo)]
                {:infinitivo infinitivo
                 :concepto   (:concepto v)
                 :persona    persona
                 :numero     (keyword numero)
                 :tiempo     (keyword tiempo)}))))
        terminaciones))

(defn- con-alternativa
  "Sin tildes (D-003) algunas formas de los verbos en -ar se leen de dos maneras:
  \"envio\" = envío (yo, presente) o envió (él, pasado), y \"esperamos\" =
  presente o pasado (D-015). La morfología deja el presente y anota la otra
  lectura en :alternativa; el parser escoge según el sujeto."
  [lexema {:keys [infinitivo tiempo persona numero] :as v}]
  (let [raiz (when (str/ends-with? infinitivo "ar") (subs infinitivo 0 (- (count infinitivo) 2)))]
    (cond
      (and raiz (= lexema (str raiz "o")) (= tiempo :presente) (= persona 1))
      (assoc v :alternativa {:tiempo :pasado :persona 3 :numero :singular})

      (and raiz (= lexema (str raiz "amos")) (= tiempo :presente) (= numero :plural))
      (assoc v :alternativa {:tiempo :pasado :persona 1 :numero :plural}
               :aviso (str "Se interpretó '" lexema "' en presente (D-015)."))

      :else v)))

(defn analizar-verbo
  "Análisis verbal de una palabra, o nil si no es un verbo que la BD conozca."
  ([lexema] (analizar-verbo lexema (terminaciones-ordenadas)))
  ([lexema terminaciones]
   (when-let [v (or (bd/verbo-irregular lexema)
                    (por-terminacion lexema terminaciones))]
     (when (:concepto v)
       (con-alternativa lexema v)))))

;; ---------------------------------------------------------------------------
;; Sustantivos en plural
;; ---------------------------------------------------------------------------

(defn- singular [lexema]
  (some (fn [quitar]
          (when (and (str/ends-with? lexema quitar) (> (count lexema) (count quitar)))
            (let [s (subs lexema 0 (- (count lexema) (count quitar)))
                  info (bd/buscar-es s)]
              (when (= :sustantivo (:categoria info)) info))))
        ["es" "s"]))

(defn- buscar-palabra
  "Lo que la BD sabe de la palabra, con :plural? para los sustantivos."
  [lexema]
  (if-let [info (bd/buscar-es lexema)]
    (cond-> info
      (= :sustantivo (:categoria info)) (assoc :plural? (= (:concepto (singular lexema)) (:concepto info))))
    (when-let [info (singular lexema)]
      (assoc info :plural? true))))

(defn reconocible?
  "¿La morfología sabe qué es esta palabra? La usa el corrector para no
  \"corregir\" conjugaciones y plurales que sí son válidos."
  [lexema]
  (boolean (or (buscar-palabra lexema) (analizar-verbo lexema))))

;; ---------------------------------------------------------------------------
;; Unidades de varias palabras
;; ---------------------------------------------------------------------------

(defn- perifrasis
  "\"ir a + infinitivo\" (\"van a llegar\") → un solo verbo en futuro.
  Las formas de \"ir\" salen de la tabla verbo_irregular."
  [t1 t2 t3]
  (when (and (= :palabra (:tipo t1) (:tipo t2) (:tipo t3)) (= "a" (:lexema t2)))
    (let [ir (bd/verbo-irregular (:lexema t1))
          v  (verbo-en-bd (:lexema t3))]
      (when (and v (= "ir" (:infinitivo ir)))
        {:tipo      :palabra
         :lexema    (str (:lexema t1) " a " (:lexema t3))
         :pos       {:inicio (get-in t1 [:pos :inicio]) :fin (get-in t3 [:pos :fin])}
         :categoria :verbo
         :concepto  (:concepto v)
         :verbo     {:infinitivo  (:lexema t3)
                     :concepto    (:concepto v)
                     :persona     (:persona ir)
                     :numero      (:numero ir)
                     :tiempo      :futuro
                     :perifrasis? true}}))))

(defn- territorio-reclamado
  "\"territorio(s) reclamado(s)\" → un solo sustantivo (D-005)."
  [t1 t2]
  (when (and (#{"territorio" "territorios"} (:lexema t1))
             (#{"reclamado" "reclamados"} (:lexema t2)))
    {:tipo      :palabra
     :lexema    (str (:lexema t1) " " (:lexema t2))
     :pos       {:inicio (get-in t1 [:pos :inicio]) :fin (get-in t2 [:pos :fin])}
     :categoria :sustantivo
     :concepto  :territorio-reclamado
     :plural?   (= "territorios" (:lexema t1))}))

;; ---------------------------------------------------------------------------
;; Punto de entrada
;; ---------------------------------------------------------------------------

(defn- analizar-palabra [token terminaciones]
  (let [lexema (:lexema token)]
    (if-let [v (analizar-verbo lexema terminaciones)]
      (assoc token :categoria :verbo :concepto (:concepto v) :verbo v)
      (if-let [info (buscar-palabra lexema)]
        (cond-> (assoc token :categoria (:categoria info) :concepto (:concepto info))
          (= :pronombre (:categoria info)) (assoc :pronombre (persona-de-pronombre (:concepto info)))
          (= :sustantivo (:categoria info)) (assoc :plural? (:plural? info)))
        (assoc token :categoria :desconocida)))))

(defn analizar
  "Agrega a cada palabra su categoría y su análisis. Una palabra que la BD no
  conoce queda como :desconocida (D-014); no se inventa nada."
  [tokens]
  (let [terminaciones (terminaciones-ordenadas)]
    (loop [ts tokens, out []]
      (if (empty? ts)
        out
        (let [[t1 t2 t3] ts]
          (if-let [p (and t3 (perifrasis t1 t2 t3))]
            (recur (drop 3 ts) (conj out p))
            (if-let [tr (and t2 (territorio-reclamado t1 t2))]
              (recur (drop 2 ts) (conj out tr))
              (recur (rest ts)
                     (conj out (if (= :palabra (:tipo t1))
                                 (analizar-palabra t1 terminaciones)
                                 t1))))))))))
