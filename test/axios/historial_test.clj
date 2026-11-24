(ns axios.historial-test
  "Pruebas del registro de preguntas en la BD y del historial (PP-32).
  Responsable: Vanessa. Cada prueba trabaja en una BD temporal."
  (:require [axios.bd :as bd]
            [axios.errores :as errores]
            [axios.parser :as parser]
            [axios.pipeline :as pipeline]
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

(deftest una-pregunta-aprobada-queda-completa-en-la-bd
  (let [r (pipeline/traducir "¿Cuándo vendrán ustedes?")
        [h] (bd/historial)]
    (testing "el id que devuelve el pipeline es el de la BD"
      (is (= (:id r) (:id h))))
    (testing "se guarda la pregunta tal como llegó, normalizada, con su árbol y su estado"
      (is (= "¿Cuándo vendrán ustedes?" (:original h)))
      (is (= "¿cuando vendran ustedes?" (:normalizado h)))
      (is (= (:arbol r) (:arbol h)))
      (is (= :aprobada (:estado h)))
      (is (= (:fecha r) (:fecha h))))
    (testing "se guardan las dos traducciones con su validación"
      (is (= (get-in r [:traducciones :kethari :texto]) (get-in h [:traducciones :kethari :texto])))
      (is (= (get-in r [:traducciones :ozh :texto]) (get-in h [:traducciones :ozh :texto])))
      (is (true? (get-in h [:traducciones :kethari :valida?]))))
    (testing "cada palabra generada queda unida a su fila de la BD, con su fuente (no se inventa nada)"
      (let [ps (get-in h [:traducciones :kethari :palabras])]
        (is (seq ps))
        (is (every? :fuente-id ps))))))

(deftest una-pregunta-rechazada-tambien-se-guarda
  (with-redefs [parser/analizar
                (fn [_] (errores/error-de-fase :parser :palabra-desconocida
                                               "'en' no tiene equivalente en Kethari ni en Ozh." {}))]
    (let [r (pipeline/traducir "¿Están en su planeta?")
          h (last (bd/historial))]
      (is (= :rechazada (:estado h)))
      (is (= (:explicacion r) (:explicacion h)))
      (is (string? (:explicacion h)))
      (is (= {} (:traducciones h))))))

(deftest se-guardan-las-correcciones-y-los-avisos
  (let [id (bd/registrar-pregunta!
            {:fecha        "2026-11-24T10:00:00"
             :original     "¿Cuándo lleagn?"
             :normalizado  {:texto "¿cuando lleagn?"}
             :correcciones [{:original "lleagn" :corregida "llegan" :distancia 2
                             :pos {:inicio 8 :fin 14}}]
             :estado       :rechazada
             :explicacion  "prueba"
             :avisos       ["Se interpretó 'llegan' como 'ustedes llegan' (D-010)."]})
        h (last (bd/historial))]
    (is (= id (:id h)))
    (is (= [{:original "lleagn" :corregida "llegan" :distancia 2}] (:correcciones h)))
    (is (= ["Se interpretó 'llegan' como 'ustedes llegan' (D-010)."] (:avisos h)))
    (is (= [{:pos-inicio 8 :pos-fin 14}]
           (consultar "SELECT pos_inicio, pos_fin FROM correccion WHERE pregunta_id = ?" id)))))

(deftest el-historial-filtra-desde-una-fecha
  (bd/registrar-pregunta! {:fecha "2026-11-20T10:00:00" :original "vieja" :estado :rechazada
                           :explicacion "prueba" :avisos []})
  (bd/registrar-pregunta! {:fecha "2026-11-22T10:00:00" :original "nueva" :estado :rechazada
                           :explicacion "prueba" :avisos []})
  (is (= ["vieja" "nueva"] (map :original (bd/historial))))
  (is (= ["nueva"] (map :original (bd/historial "2026-11-21"))))
  (is (= [] (bd/historial "2026-12-01"))))

(deftest el-historial-sobrevive-al-cerrar-el-programa
  (testing "lo registrado está en la tabla pregunta, no en memoria"
    (pipeline/traducir "¿Cuándo vendrán ustedes?")
    (is (= 1 (:n (first (consultar "SELECT COUNT(*) AS n FROM pregunta")))))
    (is (= 2 (:n (first (consultar "SELECT COUNT(*) AS n FROM traduccion")))))))
