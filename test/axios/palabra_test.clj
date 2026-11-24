(ns axios.palabra-test
  "Pruebas de la orden `palabra` (PP-33). Responsable: Vanessa.
  Cada prueba trabaja en una BD temporal."
  (:require [axios.bd :as bd]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(defn- bd-temporal [f]
  (let [archivo (java.io.File/createTempFile "axios-prueba" ".db")]
    (.delete archivo)
    (try
      (binding [bd/*ruta-bd* (.getPath archivo)]
        (f))
      (finally (.delete archivo)))))

(use-fixtures :each bd-temporal)

(defn- resumen
  "Solo lo que se quiere comparar: lengua, significado y vigencia."
  [info]
  (mapv #(select-keys % [:lengua :significado :desde :hasta]) (:significados info)))

(deftest teka-antes-y-despues-de-la-correccion-d002
  (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-23")
  (testing "sin fecha: los dos significados, cada uno con su fecha"
    (is (= [{:lengua :kethari :significado "tierra" :desde "2026-11-10" :hasta "2026-11-23"}
            {:lengua :kethari :significado "territorio reclamado" :desde "2026-11-23" :hasta nil}]
           (resumen (bd/palabra-info "teka" nil)))))
  (testing "con fecha: solo lo que estaba vigente ese día"
    (is (= ["tierra"] (map :significado (:significados (bd/palabra-info "teka" "2026-11-21")))))
    (is (= ["territorio reclamado"] (map :significado (:significados (bd/palabra-info "teka" "2026-11-23")))))
    (is (= [] (:significados (bd/palabra-info "teka" "2026-11-01")))))
  (testing "cada versión dice de dónde salió"
    (is (= ["Manual de decodificación v0.7" "Correcciones de senales_semana2.txt (manual 0.8)"]
           (map :fuente (:significados (bd/palabra-info "teka" nil)))))))

(deftest equivalentes-en-las-tres-lenguas
  (testing "desde el español"
    (let [[s] (:significados (bd/palabra-info "nave" nil))]
      (is (= :es (:lengua s)))
      (is (= ["kum"] (map :forma (get-in s [:equivalentes :kethari]))))
      (is (= ["gor"] (map :forma (get-in s [:equivalentes :ozh]))))))
  (testing "desde Kethari y desde Ozh, con caracteres especiales"
    (is (= "planeta" (:significado (first (:significados (bd/palabra-info "vael" nil))))))
    (is (= "planeta" (:significado (first (:significados (bd/palabra-info "thüm" nil))))))
    (is (= "nosotros" (:significado (first (:significados (bd/palabra-info "ʔenka" nil)))))))
  (testing "el español se busca sin tildes, como lo guarda el vocabulario (D-003)"
    (is (= :cuando (:concepto (first (:significados (bd/palabra-info "Cuándo" nil))))))))

(deftest tierra-en-kethari-despues-de-la-correccion
  (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-23")
  (let [kethari (fn [fecha] (get-in (first (:significados (bd/palabra-info "tierra" fecha)))
                                    [:equivalentes :kethari]))]
    (is (= [{:forma "teka" :desde "2026-11-10" :hasta "2026-11-23"}]
           (map #(dissoc % :fuente) (kethari nil))))
    (is (= ["teka"] (map :forma (kethari "2026-11-20"))))
    (is (nil? (kethari "2026-11-24")))))

(deftest termino-desconocido-y-fecha-invalida
  (is (= [] (:significados (bd/palabra-info "zzz" nil))))
  (is (= :fecha-invalida
         (:tipo (try (bd/palabra-info "teka" "21/11/2026") nil
                     (catch clojure.lang.ExceptionInfo e (ex-data e)))))))
