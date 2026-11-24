(ns axios.corrector-test
  (:require [clojure.test :refer [deftest testing is]]
            [axios.corrector :as corrector]))

(deftest test-levenshtein
  (testing "Calcula distancias correctamente"
    (is (= 0 (corrector/levenshtein "hola" "hola")))
    (is (= 1 (corrector/levenshtein "hola" "ola")))
    (is (= 1 (corrector/levenshtein "hola" "holas")))
    (is (= 1 (corrector/levenshtein "hola" "hola2")))
    (is (= 2 (corrector/levenshtein "lleagn" "llegan")))
    (is (= 5 (corrector/levenshtein "casa" "perro")))))

(deftest test-corregir
  (let [vocab #{"cuando" "llegan" "mundo" "hola" "naves" "quince"}
        tokens [{:tipo :palabra :lexema "cuando" :pos {:inicio 0 :fin 6}}
                {:tipo :palabra :lexema "lleagn" :pos {:inicio 7 :fin 13}}
                {:tipo :palabra :lexema "mundo" :pos {:inicio 14 :fin 19}}
                {:tipo :signo :lexema "?" :pos {:inicio 19 :fin 20}}]
        res (corrector/corregir tokens vocab)]
    (testing "Corrige palabras con distancia <= 2"
      (is (= "cuando" (:lexema (first (:tokens res)))))
      (is (= "llegan" (:lexema (second (:tokens res)))))
      (is (= "mundo" (:lexema (nth (:tokens res) 2))))
      (is (= 1 (count (:correcciones res))))
      (is (= "lleagn" (:original (first (:correcciones res)))))
      (is (= "llegan" (:corregida (first (:correcciones res)))))
      (is (= 2 (:distancia (first (:correcciones res))))))))

(deftest test-palabra-valida-no-cambia
  (let [vocab #{"hola" "mundo" "naves" "llegan"}
        tokens [{:tipo :palabra :lexema "naves" :pos {:inicio 0 :fin 5}}
                {:tipo :palabra :lexema "llegan" :pos {:inicio 6 :fin 12}}]
        res (corrector/corregir tokens vocab)]
    (testing "Las palabras ya válidas no se tocan"
      (is (empty? (:correcciones res)))
      (is (= "naves" (:lexema (first (:tokens res)))))
      (is (= "llegan" (:lexema (second (:tokens res))))))))

(deftest test-palabra-desconocida-sin-correccion
  (let [vocab #{"hola" "mundo"}
        tokens [{:tipo :palabra :lexema "xyz" :pos {:inicio 0 :fin 3}}]
        res (corrector/corregir tokens vocab)]
    (testing "Si no hay corrección aceptable, se deja igual"
      (is (empty? (:correcciones res)))
      (is (= "xyz" (:lexema (first (:tokens res))))))))
;; --- Correcciones de la segunda revisión ---

(deftest test-umbral-segun-largo
  (testing "Palabras de hasta 5 letras: distancia 1 (D-022). armas no se vuelve aguas"
    (let [res (corrector/corregir [{:tipo :palabra :lexema "armas" :pos {:inicio 0 :fin 5}}] #{"aguas"})]
      (is (empty? (:correcciones res))))))

(deftest test-no-toca-conjugaciones-validas
  (testing "Lo que la morfología reconoce no se corrige aunque no esté en el vocabulario"
    (let [res (corrector/corregir [{:tipo :palabra :lexema "visitaran" :pos {:inicio 0 :fin 9}}
                                   {:tipo :palabra :lexema "estan" :pos {:inicio 10 :fin 15}}]
                                  #{"visitaron" "estar"})]
      (is (empty? (:correcciones res))))))

(deftest test-empate-mismo-largo
  (let [res (corrector/corregir [{:tipo :palabra :lexema "lleagn" :pos {:inicio 0 :fin 6}}] #{"llega" "llegan"})]
    (is (= "llegan" (:corregida (first (:correcciones res)))))))
