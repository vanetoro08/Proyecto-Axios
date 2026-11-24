(ns axios.morfologia
  "Análisis morfológico del español (PP-16).
  Contrato: (analizar tokens) → tokens con :categoria y :verbo (si aplica).
  Responsable: Luisa (E1).
  Consume las funciones de la BD."
  (:require [axios.bd :as bd]
            [clojure.string :as str]))

(defn- analizar-verbo
  "Dado un token de tipo :palabra, intenta analizarlo como verbo.
  Retorna el mapa con la info del verbo o nil."
  [lexema]
  ;; 1. Primero buscar en irregulares
  (if-let [v (bd/verbo-irregular lexema)]
    v
    ;; 2. Si no, intentar con terminaciones regulares
    (let [terminaciones (bd/terminaciones-es)]
      (some (fn [{:keys [terminacion conjugacion tiempo persona numero]}]
              (when (str/ends-with? lexema terminacion)
                ;; Heurística básica: quitar terminación y asumir infinitivo
                (let [raiz (subs lexema 0 (- (count lexema) (count terminacion)))
                      inf (str raiz conjugacion)]
                  {:infinitivo inf
                   :concepto (keyword inf)
                   :persona persona
                   :numero (keyword numero)
                   :tiempo (keyword tiempo)})))
            terminaciones))))

(defn- es-ir-a-infinitivo? [t1 t2 t3]
  (and (= (:tipo t1) :palabra)
       (contains? #{"voy" "vas" "va" "vamos" "van"} (:lexema t1))
       (= (:tipo t2) :palabra)
       (= (:lexema t2) "a")
       (= (:tipo t3) :palabra)))

(defn- es-territorio-reclamado? [t1 t2]
  (and (= (:tipo t1) :palabra)
       (= (:lexema t1) "territorio")
       (= (:tipo t2) :palabra)
       (contains? #{"reclamado" "reclamados"} (:lexema t2))))

(defn analizar
  "Analiza los tokens agregando :categoria y :verbo."
  [tokens]
  (loop [tokens-rest tokens
         tokens-out []]
    (if (empty? tokens-rest)
      tokens-out
      (let [t1 (first tokens-rest)
            t2 (second tokens-rest)
            t3 (nth tokens-rest 2 nil)
            t4 (nth tokens-rest 3 nil)]
        (cond
          ;; Caso 1: Perífrasis "ir a + infinitivo" (ej. "van a llegar")
          (and t3 (es-ir-a-infinitivo? t1 t2 t3))
          (recur (drop 3 tokens-rest)
                 (conj tokens-out {:tipo :palabra
                                   :lexema (str (:lexema t1) " a " (:lexema t3))
                                   :categoria :verbo
                                   :verbo {:infinitivo (:lexema t3)
                                           :concepto (keyword (:lexema t3))
                                           :persona 3 ; Heurística: "van a llegar" es plural
                                           :numero :plural
                                           :tiempo :futuro
                                           :perifrasis? true}}))

          ;; Caso 2: "territorio(s) reclamado(s)"
          (and t2 (es-territorio-reclamado? t1 t2))
          (recur (drop 2 tokens-rest)
                 (conj tokens-out {:tipo :palabra
                                   :lexema (str (:lexema t1) " " (:lexema t2))
                                   :categoria :sustantivo
                                   :concepto :territorio-reclamado}))

          ;; Caso 3: Palabra normal
          (= (:tipo t1) :palabra)
          (if-let [info-verbo (analizar-verbo (:lexema t1))]
            (recur (rest tokens-rest)
                   (conj tokens-out (assoc t1 
                                           :categoria :verbo 
                                           :verbo (assoc info-verbo 
                                                         :aviso (when (and (= (:terminacion "amos") "amos") 
                                                                           (str/ends-with? (:lexema t1) "amos"))
                                                                  "D-015: primera persona plural, presente por defecto")))))
            ;; Si no es verbo, buscar en la BD
            (if-let [info-bd (bd/buscar-es (:lexema t1))]
              (recur (rest tokens-rest)
                     (conj tokens-out (assoc t1 :categoria (:categoria info-bd))))
              ;; Si no está en la BD, dejar como desconocida
              (recur (rest tokens-rest)
                     (conj tokens-out (assoc t1 :categoria :desconocida)))))

          ;; Caso 4: Otros tokens (signos, números)
          :else
          (recur (rest tokens-rest) (conj tokens-out t1)))))))