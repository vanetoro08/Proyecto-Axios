(ns axios.morfologia
  "Análisis morfológico del español (PP-16).
  Contrato: (analizar tokens) → tokens con :categoria y :verbo (si aplica).
  Responsable: Luisa (E1)."
  (:require [axios.bd :as bd]
            [clojure.string :as str]))

(defn- analizar-verbo [lexema]
  ;; 1. Buscar en irregulares
  (if-let [v (bd/verbo-irregular lexema)]
    v
    ;; 2. Si no es irregular, buscar en la BD. Si es sustantivo/pronombre, NO intentar conjugarlo.
    (if-let [info-bd (bd/buscar-es lexema)]
      (if (= (:categoria info-bd) :verbo)
        (let [terminaciones (bd/terminaciones-es)]
          (some (fn [{:keys [terminacion conjugacion tiempo persona numero]}]
                  (when (str/ends-with? lexema terminacion)
                    (let [raiz (subs lexema 0 (- (count lexema) (count terminacion)))
                          inf (str raiz conjugacion)]
                      {:infinitivo inf
                       :concepto (:concepto info-bd) ;; CORRECCIÓN: Mapear a :venir, :enviar, etc.
                       :persona persona
                       :numero (keyword numero)
                       :tiempo (keyword tiempo)})))
                terminaciones))
        nil) ;; Es un sustantivo o pronombre conocido, no es verbo.
      ;; 3. Si no está en BD, intentar heurística (con cuidado)
      (let [terminaciones (bd/terminaciones-es)]
        (some (fn [{:keys [terminacion conjugacion tiempo persona numero]}]
                (when (str/ends-with? lexema terminacion)
                  (let [raiz (subs lexema 0 (- (count lexema) (count terminacion)))
                        inf (str raiz conjugacion)]
                    {:infinitivo inf :concepto (keyword inf) :persona persona :numero (keyword numero) :tiempo (keyword tiempo)})))
              terminaciones)))))

(defn- es-ir-a-infinitivo? [t1 t2 t3]
  (and (= (:tipo t1) :palabra)
       (contains? #{"voy" "vas" "va" "vamos" "van"} (:lexema t1))
       (= (:tipo t2) :palabra) (= (:lexema t2) "a")
       (= (:tipo t3) :palabra)))

(defn analizar [tokens]
  (loop [tokens-rest tokens tokens-out []]
    (if (empty? tokens-rest)
      tokens-out
      (let [t1 (first tokens-rest) t2 (second tokens-rest) t3 (nth tokens-rest 2 nil)]
        (cond
          ;; Caso 1: Ir a + Infinitivo
          (and t3 (es-ir-a-infinitivo? t1 t2 t3))
          (let [ir-verbo (bd/verbo-irregular (:lexema t1))]
            (recur (drop 3 tokens-rest)
                   (conj tokens-out {:tipo :palabra :lexema (str (:lexema t1) " a " (:lexema t3))
                                     :categoria :verbo
                                     :verbo {:infinitivo (:lexema t3) :concepto (keyword (:lexema t3))
                                             :persona (:persona ir-verbo) :numero (:numero ir-verbo)
                                             :tiempo :futuro :perifrasis? true}})))

          ;; Caso 2: Territorio reclamado
          (and t2 (= (:lexema t1) "territorio") (contains? #{"reclamado" "reclamados"} (:lexema t2)))
          (recur (drop 2 tokens-rest) (conj tokens-out {:tipo :palabra :lexema (str (:lexema t1) " " (:lexema t2)) :categoria :sustantivo :concepto :territorio-reclamado}))

          ;; Caso 3: Palabra normal
          (= (:tipo t1) :palabra)
          (if-let [info-verbo (analizar-verbo (:lexema t1))]
            (recur (rest tokens-rest)
                   (conj tokens-out (assoc t1 :categoria :verbo
                                           :verbo (assoc info-verbo
                                                         :aviso (when (and (= (:tiempo info-verbo) :presente)
                                                                           (= (:persona info-verbo) 1)
                                                                           (= (:numero info-verbo) :plural)
                                                                           (str/ends-with? (:lexema t1) "amos"))
                                                                  "D-015: primera persona plural, presente por defecto")))))
            (if-let [info-bd (bd/buscar-es (:lexema t1))]
              (let [token-nuevo (assoc t1 :categoria (:categoria info-bd))]
                (recur (rest tokens-rest)
                       (conj tokens-out
                             (cond
                               (= (:categoria info-bd) :pronombre) (assoc token-nuevo :pronombre {:persona 3 :numero :plural})
                               (= (:categoria info-bd) :sustantivo) (assoc token-nuevo :concepto (:concepto info-bd))
                               :else token-nuevo))))
              (recur (rest tokens-rest) (conj tokens-out (assoc t1 :categoria :desconocida)))))

          :else (recur (rest tokens-rest) (conj tokens-out t1)))))))