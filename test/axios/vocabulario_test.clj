(ns axios.vocabulario-test
  (:require [clojure.test :refer [deftest testing is]]
            [axios.bd :as bd]))

(deftest test-vocabulario-cargado
  (testing "El vocabulario contiene las palabras base del manual"
    (let [vocab (bd/vocabulario-es)]
      (is (contains? vocab "yo"))
      (is (contains? vocab "nave"))
      (is (contains? vocab "venir"))))

  (testing "El vocabulario contiene las palabras extendidas de PP-14"
    (let [vocab (bd/vocabulario-es)]
      ;; Artículos
      (is (contains? vocab "el"))
      (is (contains? vocab "la"))
      (is (contains? vocab "los"))
      ;; Posesivos
      (is (contains? vocab "mi"))
      (is (contains? vocab "nuestro"))
      ;; Preposiciones
      (is (contains? vocab "de"))
      (is (contains? vocab "en"))
      ;; Conjunciones
      (is (contains? vocab "y"))
      ;; Negación
      (is (contains? vocab "no"))
      ;; Conjugaciones
      (is (contains? vocab "vendran"))
      (is (contains? vocab "enviamos"))
      ;; Concepto compuesto
      (is (contains? vocab "territorio reclamado")))))

(deftest test-buscar-es
  (testing "Busca palabras y devuelve su concepto y categoría"
    (is (= {:concepto :venir :categoria :verbo}
           (bd/buscar-es "llegar")))
    (is (= {:concepto :nave :categoria :sustantivo}
           (bd/buscar-es "nave")))
    (is (= {:concepto :venir :categoria :verbo}
           (bd/buscar-es "vendran")))))