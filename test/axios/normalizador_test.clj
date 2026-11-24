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
;; --- Correcciones de la segunda revisión ---

(deftest test-limite-9999
  (testing "Más de 9.999 es un error de fase con tipo"
    (let [e (try (n/normalizar "diez mil naves") nil
                 (catch clojure.lang.ExceptionInfo e (ex-data e)))]
      (is (= :normalizador (:fase e)))
      (is (= :numero-fuera-de-rango (:tipo e)))))
  (testing "9.999 sí se acepta"
    (is (= "9999" (:texto (n/normalizar "nueve mil novecientos noventa y nueve"))))))

(deftest test-numero-pegado-a-signo
  (testing "El número se convierte aunque tenga un signo pegado"
    (is (= "esperamos 15." (:texto (n/normalizar "Esperamos quince."))))
    (is (= "¿cuantas naves? 15." (:texto (n/normalizar "¿Cuántas naves? Quince."))))
    (is (= "¡26 estrellas!" (:texto (n/normalizar "¡Veintiséis estrellas!"))))))

(deftest test-y-suelta
  (testing "Una y que no es parte de un número se conserva"
    (is (= "agua y 2 naves" (:texto (n/normalizar "agua y dos naves"))))
    (is (= "32 naves" (:texto (n/normalizar "treinta y dos naves"))))))

(deftest test-errores-con-tipo
  (testing "Entrada vacía y texto en Ozh traen :fase y :tipo"
    (is (= :entrada-vacia (:tipo (try (n/normalizar "  ") nil (catch clojure.lang.ExceptionInfo e (ex-data e))))))
    (is (= :texto-ozh (:tipo (try (n/normalizar "ʔasur") nil (catch clojure.lang.ExceptionInfo e (ex-data e))))))))
