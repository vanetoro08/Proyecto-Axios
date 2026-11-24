(ns axios.corrector-test
  (:require [clojure.test :refer [deftest testing is]]
            [axios.corrector :as corrector]))

(deftest test-levenshtein
  (testing "Calcula distancias correctamente"
    (is (= 0 (corrector/levenshtein "hola" "hola")))
    (is (= 1 (corrector/levenshtein "hola" "ola")))      ; Borrado
    (is (= 1 (corrector/levenshtein "hola" "holas")))    ; Inserción
    (is (= 1 (corrector/levenshtein "hola" "hola2")))    ; Sustitución
    (is (= 2 (corrector/levenshtein "lleagn" "llegan"))) ; Letras cambiadas de orden
    (is (= 5 (corrector/levenshtein "casa" "perro")))))  ; Muy diferentes (¡CORREGIDO AQUÍ!)

(deftest test-corregir
  (let [vocab #{"cuando" "llegan" "mundo" "hola" "naves" "quince"}
        tokens [{:tipo :palabra :lexema "cuando" :pos {:inicio 0 :fin 6}}  ; ok
                {:tipo :palabra :lexema "lleagn" :pos {:inicio 7 :fin 13}}  ; letras cambiadas
                {:tipo :palabra :lexema "mundo" :pos {:inicio 14 :fin 19}}  ; ok
                {:tipo :signo :lexema "?" :pos {:inicio 19 :fin 20}}]
        res (corrector/corregir tokens vocab)]
    (testing "Corrige palabras con distancia <= 2"
      (is (= "cuando" (:lexema (first (:tokens res))))) ; No se toca
      (is (= "llegan" (:lexema (second (:tokens res))))) ; Se corrige
      (is (= "mundo" (:lexema (nth (:tokens res) 2)))) ; No se toca
      (is (= 1 (count (:correcciones res)))) ; Solo una corrección
      (is (= "lleagn" (:original (first (:correcciones res)))))
      (is (= "llegan" (:corregida (first (:correcciones res)))))
      (is (= 2 (:distancia (first (:correcciones res))))))))

(deftest test-palabra-valida-no-cambia
  (let [vocab #{"hola" "mundo"}
        tokens [{:tipo :palabra :lexema "mundo" :pos {:inicio 0 :fin 5}}]
        res (corrector/corregir tokens vocab)]
    (testing "Las palabras ya válidas no se tocan"
      (is (empty? (:correcciones res)))
      (is (= "mundo" (:lexema (first (:tokens res)))))
      (is (nil? (:original (first (:tokens res))))))))

(deftest test-palabra-desconocida-sin-correccion
  (let [vocab #{"hola" "mundo"}
        tokens [{:tipo :palabra :lexema "xyz" :pos {:inicio 0 :fin 3}}]
        res (corrector/corregir tokens vocab)]
    (testing "Si no hay corrección aceptable, se deja igual"
      (is (empty? (:correcciones res)))
      (is (= "xyz" (:lexema (first (:tokens res))))))))