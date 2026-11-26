(ns axios.senales-test
  "Pruebas de las señales en la BD (PP-41, bitácora D-025). Responsable: Vanessa.
  Cada prueba trabaja en una BD temporal, para no tocar axios.db."
  (:require [axios.bd :as bd]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(def ^:private consultar @#'axios.bd/consultar)

(defn- bd-temporal [f]
  (let [archivo (java.io.File/createTempFile "axios-prueba" ".db")]
    (.delete archivo)
    (try
      (binding [bd/*ruta-bd* (.getPath archivo)]
        (f))
      (finally (.delete archivo)))))

(use-fixtures :each bd-temporal)

(deftest al-crear-la-bd-estan-las-senales-de-la-semana-1
  (testing "7 señales de Ozh y 7 de Kethari con transcripción"
    (is (= 7 (count (bd/senales :ozh))))
    (is (= 7 (count (bd/senales :kethari)))))
  (testing "vienen en orden de día y con el formato del contrato"
    (is (= {:dia 1 :voz "ozh" :transcripcion "ʔoka dra zi ʔenka vesh"
            :salida "Antes enviamos un mensaje. [zi]"}
           (dissoc (first (bd/senales :ozh)) :id :fuente-id)))
    (is (= (range 1 8) (map :dia (bd/senales :ozh)))))
  (testing "la fuente de cada señal es la carga de la semana 1"
    (let [fuente (:fuente-id (first (bd/senales :ozh)))]
      (is (= "Señales de senales_semana1.txt (manual 0.7)"
             (:descripcion (first (consultar "SELECT descripcion FROM fuente WHERE id = ?" fuente))))))))

(deftest la-senal-sin-transcripcion-no-se-devuelve
  (testing "la del día 6 (voz desconocida) está en la BD pero no sirve para el generador"
    (is (= 1 (:n (first (consultar "SELECT COUNT(*) AS n FROM senal WHERE voz = 'desconocida'")))))
    (is (empty? (bd/senales :desconocida)))))

(deftest la-semana-2-agrega-la-senal-del-dia-9
  (testing "antes de cargar la semana 2 no hay señal del día 9"
    (is (not-any? #(= 9 (:dia %)) (bd/senales :ozh))))
  (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-23")
  (testing "después hay 10 señales de Ozh, y la del día 9 es la pregunta con tho (D-008)"
    (is (= 10 (count (bd/senales :ozh))))
    (is (= {:dia 9 :transcripcion "ʔanem tho shavi wis" :salida "¿A quién escuchan ustedes? [tho]"}
           (select-keys (first (filter #(= 9 (:dia %)) (bd/senales :ozh)))
                        [:dia :transcripcion :salida])))))
