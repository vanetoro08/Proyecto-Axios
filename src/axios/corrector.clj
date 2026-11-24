(ns axios.corrector
  "Corrector ortográfico usando distancia de Levenshtein (PP-15).
  Contrato: (corregir tokens vocabulario) → {:tokens [...] :correcciones [...]}
  Responsable: Luisa (E1).
  Umbral de distancia: bitácora D-022."
  (:require [axios.morfologia :as morfologia]))

(defn levenshtein
  "Calcula la distancia de Levenshtein entre dos strings."
  [s1 s2]
  (let [n (count s1)
        m (count s2)]
    (cond
      (zero? n) m
      (zero? m) n
      :else
      (let [matriz (make-array Object (inc n) (inc m))]
        (doseq [i (range (inc n))] (aset matriz i 0 i))
        (doseq [j (range (inc m))] (aset matriz 0 j j))
        (doseq [i (range 1 (inc n))
                j (range 1 (inc m))]
          (let [costo (if (= (.charAt s1 (dec i)) (.charAt s2 (dec j))) 0 1)]
            (aset matriz i j
                  (min (inc (aget matriz (dec i) j))
                       (inc (aget matriz i (dec j)))
                       (+ costo (aget matriz (dec i) (dec j)))))))
        (aget matriz n m)))))

(defn- distancia-maxima
  "Umbral (D-022): 1 para palabras de hasta 5 letras, 2 para las más largas.
  Con 2 en palabras cortas, \"armas\" se volvía \"aguas\" y cambiaba el sentido."
  [lexema]
  (if (<= (count lexema) 5) 1 2))

(defn- corregir-palabra
  "La palabra del vocabulario más cercana a `lexema` dentro del umbral, o nil.
  En un empate gana la de largo más parecido (\"lleagn\" → \"llegan\", no
  \"llega\") y después el orden alfabético, para que el resultado no cambie de
  una ejecución a otra."
  [lexema vocabulario]
  (let [maximo (distancia-maxima lexema)
        n      (count lexema)]
    (->> vocabulario
         (keep (fn [pal]
                 (let [dist (levenshtein lexema pal)]
                   (when (<= dist maximo)
                     {:corregida pal :distancia dist}))))
         (sort-by (fn [{:keys [corregida distancia]}]
                    [distancia (abs (- (count corregida) n)) corregida]))
         first)))

(defn- valida?
  "Una palabra es válida si está en el vocabulario o si la morfología la
  reconoce (conjugaciones y plurales que no están escritos uno por uno)."
  [lexema vocabulario]
  (or (contains? vocabulario lexema)
      (morfologia/reconocible? lexema)))

(defn corregir
  "Corrige solo las palabras que no son válidas; nunca toca una válida."
  [tokens vocabulario]
  (loop [tokens-rest tokens
         tokens-out []
         correcciones []]
    (if (empty? tokens-rest)
      {:tokens tokens-out :correcciones correcciones}
      (let [token (first tokens-rest)]
        (if (and (= (:tipo token) :palabra)
                 (not (valida? (:lexema token) vocabulario)))
          (let [lexema (:lexema token)
                mejor (corregir-palabra lexema vocabulario)]
            (if mejor
              (recur (rest tokens-rest)
                     (conj tokens-out (assoc token :lexema (:corregida mejor) :original lexema))
                     (conj correcciones {:original lexema
                                         :corregida (:corregida mejor)
                                         :distancia (:distancia mejor)
                                         :pos (:pos token)}))
              (recur (rest tokens-rest) (conj tokens-out token) correcciones)))
          (recur (rest tokens-rest) (conj tokens-out token) correcciones))))))
