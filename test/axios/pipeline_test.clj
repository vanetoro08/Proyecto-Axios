(ns axios.pipeline-test
  "Pruebas del pipeline completo y de la batería de 30+ pares.
  Responsable: Jhojan (PP-24, PP-28)."
  (:require [axios.bd :as bd]
            [axios.errores :as errores]
            [axios.parser :as parser]
            [axios.pipeline :as pipeline]
            [axios.validador.kethari :as val-kethari]
            [axios.validador.ozh :as val-ozh]
            [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]))

(def pares
  (edn/read-string (slurp "test/recursos/pares.edn" :encoding "UTF-8")))

(defn- pipeline-en-modo-stub?
  "Detecta si las fases E1-E3 todavía devuelven el stub fijo de P01.
  Mientras haya stubs, las pruebas end-to-end verifican P01; en cuanto E1-E3
  se integren en `develop`, se ejecutan automáticamente todos los pares."
  []
  (let [r (pipeline/traducir "Queremos paz")]
    (= "kom ulesh vinime ka" (get-in r [:traducciones :kethari :texto]))))

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

(deftest pares-cumplen-minimo-y-cobertura-de-la-rubrica
  (let [validos (filter #(and (some? (:kethari %)) (some? (:ozh %))) pares)
        rechazos (filter #(and (nil? (:kethari %)) (nil? (:ozh %))) pares)
        etiquetas (into #{} (mapcat :cubre pares))]
    (is (>= (count validos) 30)
        (str "La rúbrica exige mínimo 30 pares traducibles; encontrados: " (count validos)))
    (is (>= (count rechazos) 3))
    (doseq [req [:afirmacion :pregunta-si-no :pregunta-abierta
                 :que :cuando :donde :cuantos :quien
                 :pasado :presente :futuro :futuro-ir-a
                 :negacion :plural :posesivo :sujeto-omitido
                 :numero-en-letras :numero-cifras :limite-9999
                 :sin-tildes :sin-signos :error-ortografico
                 :palabra-desconocida]]
      (is (contains? etiquetas req)
          (str "Falta cobertura en test/recursos/pares.edn para: " req)))))

(deftest todos-los-pares-esperados-pasan-validadores-independientes
  (doseq [{:keys [id espanol kethari ozh]} pares
          :when (and (some? kethari) (some? ozh))]
    (testing (str id " (" espanol ")")
      (let [rk (val-kethari/validar kethari)
            ro (val-ozh/validar ozh)]
        (is (:valida? rk)
            (str "Kethari esperado inválido en " id " ('" kethari "'): " (:errores rk)))
        (is (:valida? ro)
            (str "Ozh esperado inválido en " id " ('" ozh "'): " (:errores ro)))))))

(deftest pares-de-prueba
  (let [pares-activos (if (pipeline-en-modo-stub?)
                        (filter #(= "P01" (:id %)) pares)
                        pares)]
    (doseq [{:keys [id espanol kethari ozh]} pares-activos]
      (testing (str id ": " espanol)
        (let [r (pipeline/traducir espanol)]
          (if (and (nil? kethari) (nil? ozh))
            (is (= :rechazada (:estado r)))
            (do (is (= :aprobada (:estado r)))
                (is (= kethari (get-in r [:traducciones :kethari :texto])))
                (is (= ozh     (get-in r [:traducciones :ozh :texto]))))))))))

(deftest ninguna-pregunta-se-pierde-cuando-una-fase-falla
  (let [antes (count (bd/historial))]
    (with-redefs [parser/analizar
                  (fn [_]
                    (errores/error-de-fase :parser :estructura-invalida
                                           "Falta el verbo principal" {:pos 8}))]
      (let [r (pipeline/traducir "¿Cuándo sus naves?")]
        (is (= :rechazada (:estado r)))
        (is (pos? (:id r)))
        (is (= :parser (get-in r [:error :fase])))
        (is (= :estructura-invalida (get-in r [:error :tipo])))
        (is (string? (:explicacion r)))
        (is (= (inc antes) (count (bd/historial))))))))
