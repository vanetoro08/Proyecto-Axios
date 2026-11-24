(ns axios.corrector
  "Corrector ortográfico usando distancia de Levenshtein (PP-15).
  Contrato: (corregir tokens vocabulario) → {:tokens [...] :correcciones [...]}
  Responsable: Luisa (E1).
  Nota: Recibe el vector de tokens del lexer y el set de palabras válidas de la BD."
  (:require [clojure.string :as str]))

(defn levenshtein
  "Calcula la distancia de Levenshtein entre dos strings usando programación dinámica.
  Distancia 0 = iguales. Distancia 1 = un cambio (inserción, borrado o sustitución)."
  [s1 s2]
  (let [n (count s1)
        m (count s2)]
    (cond
      (zero? n) m
      (zero? m) n
      :else
      (let [matriz (make-array Object (inc n) (inc m))]
        ;; Inicializar primera fila y columna
        (doseq [i (range (inc n))] (aset matriz i 0 i))
        (doseq [j (range (inc m))] (aset matriz 0 j j))
        ;; Llenar la matriz
        (doseq [i (range 1 (inc n))
                j (range 1 (inc m))]
          (let [costo (if (= (.charAt s1 (dec i)) (.charAt s2 (dec j))) 0 1)]
            (aset matriz i j
                  (min (inc (aget matriz (dec i) j))           ; Borrado
                       (inc (aget matriz i (dec j)))           ; Inserción
                       (+ costo (aget matriz (dec i) (dec j))))))) ; Sustitución
        (aget matriz n m)))))

(defn- corregir-palabra
  "Busca en el vocabulario la palabra más cercana a `lexema`.
  Retorna {:corregida string :distancia int} o nil si no hay ninguna <= 2."
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
  "Corrige los tokens que no están en el vocabulario.
  No altera tokens que ya son válidos.
  Devuelve {:tokens [...] :correcciones [...]}."
  [tokens vocabulario]
  (loop [tokens-rest tokens
         tokens-out []
         correcciones []]
    (if (empty? tokens-rest)
      {:tokens tokens-out :correcciones correcciones}
      (let [token (first tokens-rest)]
        (if (and (= (:tipo token) :palabra)
                 (not (contains? vocabulario (:lexema token))))
          ;; Es una palabra desconocida, buscar corrección
          (let [lexema (:lexema token)
                mejor (corregir-palabra lexema vocabulario)]
            (if mejor
              ;; Encontramos corrección
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
              ;; No hay corrección aceptable, se deja igual
              (recur (rest tokens-rest)
                     (conj tokens-out token)
                     correcciones)))
          ;; Ya está en el vocabulario o no es palabra
          (recur (rest tokens-rest)
                 (conj tokens-out token)
                 correcciones))))))