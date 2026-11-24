(ns axios.normalizador-test
  (:require [clojure.test :refer [deftest testing is]]
            [axios.normalizador :as n]))

(deftest test-tildes
  (testing "Quita tildes y convierte ñ"
    (is (= "cuando" (:texto (n/normalizar "Cuándo"))))
    (is (= "ano" (:texto (n/normalizar "año"))))
    (is (= "verguenza" (:texto (n/normalizar "vergüenza"))))))

(deftest test-numeros
  (testing "Convierte números en letras"
    (is (= "15 naves" (:texto (n/normalizar "quince naves"))))
    (is (= "2015" (:texto (n/normalizar "dos mil quince"))))
    (is (= "1000 naves" (:texto (n/normalizar "mil naves"))))
    (is (= "1500 naves" (:texto (n/normalizar "mil quinientos naves"))))
    (is (= "paz y agua" (:texto (n/normalizar "paz y agua"))))))

(deftest test-pregunta
  (testing "Detecta preguntas"
    (is (true? (:pregunta? (n/normalizar "¿Cuándo vendrán?"))))
    (is (true? (:pregunta? (n/normalizar "cuando vendran"))))
    (is (false? (:pregunta? (n/normalizar "queremos paz"))))))

(deftest test-contrato
  (testing "Devuelve el mapa del contrato"
    (let [r (n/normalizar "¿Cuándo vendrán?")]
      (is (= "¿Cuándo vendrán?" (:original r)))
      (is (= "¿cuando vendran?" (:texto r)))
      (is (true? (:pregunta? r))))))