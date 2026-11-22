(ns axios.pipeline-test
  "Pruebas del pipeline completo.
  Responsable: Jhojan (PP-24, PP-28)."
  (:require [axios.pipeline :as pipeline]
            [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]))

(def pares
  (edn/read-string (slurp "test/recursos/pares.edn")))

(deftest resultado-tiene-el-formato-del-contrato
  (let [r (pipeline/traducir "¿Cuando vendran ustedes?")]
    (testing "el resultado trae todas las partes que pide contratos.md (sección 9)"
      (is (every? #(contains? r %)
                  [:fecha :original :normalizado :tokens :correcciones
                   :arbol :traducciones :estado :avisos :id])))
    (testing "siempre se traduce a las dos lenguas"
      (is (some? (get-in r [:traducciones :kethari :texto])))
      (is (some? (get-in r [:traducciones :ozh :texto]))))
    (testing "toda pregunta queda registrada (condición 1)"
      (is (pos? (:id r))))))

(deftest pares-de-prueba
  (doseq [{:keys [id espanol kethari ozh]} pares]
    (testing (str id ": " espanol)
      (let [r (pipeline/traducir espanol)]
        (if (and (nil? kethari) (nil? ozh))
          (is (= :rechazada (:estado r)))
          (do (is (= kethari (get-in r [:traducciones :kethari :texto])))
              (is (= ozh     (get-in r [:traducciones :ozh :texto])))))))))
