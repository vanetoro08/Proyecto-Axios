(ns axios.corrector
  "Corrector ortográfico usando distancia de Levenshtein (PP-15).
  Contrato: (corregir tokens vocabulario) → {:tokens [...] :correcciones [...]}
  Responsable: Luisa (E1)."
  (:require [clojure.string :as str]))

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

(defn- corregir-palabra
  "Busca en el vocabulario la palabra más cercana a `lexema`."
  [lexema vocabulario]
  (reduce (fn [mejor pal]
            (let [dist (levenshtein lexema pal)]
              (if (and (<= dist 2)
                       (or (nil? mejor) (< dist (:distancia mejor))))
                {:corregida pal :distancia dist}
                mejor)))
          nil
          vocabulario))

(defn corregir
  "Corrige los tokens que no están en el vocabulario."
  [tokens vocabulario]
  (loop [tokens-rest tokens
         tokens-out []
         correcciones []]
    (if (empty? tokens-rest)
      {:tokens tokens-out :correcciones correcciones}
      (let [token (first tokens-rest)]
        (if (and (= (:tipo token) :palabra)
                 (not (contains? vocabulario (:lexema token))))
          (let [lexema (:lexema token)
                mejor (corregir-palabra lexema vocabulario)]
            (if mejor
              (let [token-nuevo (assoc token
                                       :lexema (:corregida mejor)
                                       :original lexema)
                    corr {:original lexema
                          :corregida (:corregida mejor)
                          :distancia (:distancia mejor)
                          :pos (:pos token)}]
                (recur (rest tokens-rest)
                       (conj tokens-out token-nuevo)
                       (conj correcciones corr)))
              (recur (rest tokens-rest)
                     (conj tokens-out token)
                     correcciones)))
          (recur (rest tokens-rest)
                 (conj tokens-out token)
                 correcciones))))))