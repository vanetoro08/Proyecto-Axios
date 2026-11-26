(ns axios.core-test
  "Pruebas del modo traza (PP-35) y de lo que el pipeline guarda cuando una fase
  falla. Responsable: Vanessa. Cada prueba trabaja en una BD temporal."
  (:require [axios.bd :as bd]
            [axios.core :as core]
            [axios.parser :as parser]
            [axios.pipeline :as pipeline]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(defn- bd-temporal [f]
  (let [archivo (java.io.File/createTempFile "axios-prueba" ".db")]
    (.delete archivo)
    (try
      (binding [bd/*ruta-bd* (.getPath archivo)]
        (f))
      (finally (.delete archivo)))))

(use-fixtures :each bd-temporal)

(defn- traza [pregunta]
  (with-out-str (core/ejecutar ["traducir" pregunta "--traza"])))

(def ^:private titulos
  ["1. Normalizador" "2. Analizador léxico" "3. Corrector" "4. Morfología"
   "5. Analizador sintáctico" "6. Generadores y validadores"])

(defn- fases-mostradas [salida]
  (filterv #(str/includes? salida %) titulos))

;; ---------------------------------------------------------------------------
;; Modo traza
;; ---------------------------------------------------------------------------

(deftest la-traza-muestra-las-seis-fases
  (let [salida (traza "¿Cuándo lleagn sus quince naves?")]
    (is (= titulos (fases-mostradas salida)))
    (testing "cada fase muestra su salida"
      (is (str/includes? salida "¿cuando lleagn sus 15 naves?"))   ; normalizador
      (is (str/includes? salida "posición 8–14"))                  ; lexer, con posición
      (is (str/includes? salida "lleagn → llegan (distancia 2)"))  ; corrector
      (is (str/includes? salida "verbo llegar, presente"))         ; morfología
      (is (str/includes? salida "nave, plural, cantidad 15"))      ; árbol
      (is (str/includes? salida "Validador: la reconoce como válida")))
    (testing "después de la traza va el resultado de siempre"
      (is (str/includes? salida "Registrada con id")))))

(deftest la-traza-se-detiene-en-la-fase-que-fallo
  (testing "palabra sin equivalente: llega hasta el parser"
    (let [salida (traza "¿Están en su planeta?")]
      (is (= (subvec titulos 0 5) (fases-mostradas salida)))
      (is (str/includes? salida "se detuvo aquí (palabra-desconocida)"))
      (is (str/includes? salida "No se transmite:"))))
  (testing "carácter inválido: llega hasta el lexer"
    (let [salida (traza "¿Cuándo vienen # ustedes?")]
      (is (= (subvec titulos 0 2) (fases-mostradas salida)))
      (is (str/includes? salida "se detuvo aquí (caracter-invalido)")))))

(deftest sin-traza-solo-sale-el-resultado
  (let [salida (with-out-str (core/ejecutar ["traducir" "¿Cuándo vendrán ustedes?"]))]
    (is (empty? (fases-mostradas salida)))
    (is (str/includes? salida "Kethari:"))))

;; ---------------------------------------------------------------------------
;; Lo que guarda el pipeline cuando una fase falla
;; ---------------------------------------------------------------------------

(deftest una-pregunta-rechazada-conserva-lo-que-hicieron-las-fases-anteriores
  (let [r (pipeline/traducir "¿Están en su planeeta?")]
    (is (= :rechazada (:estado r)))
    (is (= :parser (get-in r [:error :fase])))
    (testing "quedan la normalización, los tokens y las correcciones"
      (is (= "¿estan en su planeeta?" (get-in r [:normalizado :texto])))
      (is (seq (:tokens r)))
      (is (= [["planeeta" "planeta"]] (map (juxt :original :corregida) (:correcciones r)))))
    (testing "y la corrección también queda en el historial"
      (is (= [{:original "planeeta" :corregida "planeta" :distancia 1}]
             (:correcciones (last (bd/historial))))))))

(deftest un-error-inesperado-tambien-se-registra
  (testing "si el código falla con una excepción que no es de fase, la pregunta no se pierde (condición 1)"
    (with-redefs [parser/analizar (fn [_] (throw (NullPointerException. "falla simulada")))]
      (let [r (pipeline/traducir "¿Cuándo vendrán ustedes?")]
        (is (= :rechazada (:estado r)))
        (is (= :pipeline (get-in r [:error :fase])))
        (is (= :error-interno (get-in r [:error :tipo])))
        (is (pos? (:id r)))
        (is (= :rechazada (:estado (last (bd/historial)))))))))
