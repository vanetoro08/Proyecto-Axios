(ns axios.morfologia-test
  (:require [clojure.test :refer [deftest testing is]]
            [axios.morfologia :as morfo]))

(deftest test-verbo-regular
  (testing "Analiza un verbo regular sin inventar conjugaciones"
    (let [tokens [{:tipo :palabra :lexema "esperamos" :pos {:inicio 0 :fin 9}}]
          res (morfo/analizar tokens)]
      (is (= :verbo (:categoria (first res))))
      (is (map? (:verbo (first res))))
      (is (= :presente (:tiempo (:verbo (first res)))))
      (is (= 1 (:persona (:verbo (first res)))))
      (is (= :plural (:numero (:verbo (first res)))))
      (is (string? (:aviso (:verbo (first res))))))))

(deftest test-verbo-irregular
  (testing "Analiza un verbo irregular"
    (let [tokens [{:tipo :palabra :lexema "vendran" :pos {:inicio 0 :fin 7}}]
          res (morfo/analizar tokens)]
      (is (= :verbo (:categoria (first res))))
      (is (= "venir" (:infinitivo (:verbo (first res)))))
      (is (= :futuro (:tiempo (:verbo (first res)))))
      (is (= 3 (:persona (:verbo (first res)))))
      (is (= :plural (:numero (:verbo (first res))))))))

(deftest test-sustantivo-no-es-verbo
  (testing "Un sustantivo no se intenta conjugar"
    (let [tokens [{:tipo :palabra :lexema "nave" :pos {:inicio 0 :fin 4}}]
          res (morfo/analizar tokens)]
      (is (nil? (:verbo (first res)))) ; No debe tener info de verbo
      (is (= :sustantivo (:categoria (first res)))))))

(deftest test-ir-a-infinitivo
  (testing "Juntar 'ir a + infinitivo' conservando la persona"
    (let [tokens [{:tipo :palabra :lexema "voy" :pos {:inicio 0 :fin 3}}
                  {:tipo :palabra :lexema "a" :pos {:inicio 4 :fin 5}}
                  {:tipo :palabra :lexema "llegar" :pos {:inicio 6 :fin 12}}]
          res (morfo/analizar tokens)]
      (is (= 1 (count res)))
      (is (= :verbo (:categoria (first res))))
      (is (:perifrasis? (:verbo (first res))))
      (is (= :futuro (:tiempo (:verbo (first res)))))
      (is (= 1 (:persona (:verbo (first res))))) ; "voy" es 1.ª persona singular
      (is (= :singular (:numero (:verbo (first res))))))))

(deftest test-territorio-reclamado
  (testing "Juntar 'territorio reclamado'"
    (let [tokens [{:tipo :palabra :lexema "territorio" :pos {:inicio 0 :fin 10}}
                  {:tipo :palabra :lexema "reclamado" :pos {:inicio 11 :fin 20}}]
          res (morfo/analizar tokens)]
      (is (= 1 (count res)))
      (is (= :sustantivo (:categoria (first res))))
      (is (= :territorio-reclamado (:concepto (first res)))))))
;; --- Correcciones de la segunda revisión ---

(defn- uno [& palabras]
  (first (morfo/analizar (mapv (fn [w] {:tipo :palabra :lexema w}) palabras))))

(deftest test-persona-de-pronombres
  (testing "Cada pronombre trae su persona y número (D-004, D-010, D-016)"
    (is (= {:persona 1 :numero :singular} (:pronombre (uno "yo"))))
    (is (= {:persona 1 :numero :plural}   (:pronombre (uno "nosotros"))))
    (is (= {:persona 2 :numero :plural}   (:pronombre (uno "ustedes"))))
    (is (= {:persona 3 :numero :plural}   (:pronombre (uno "ellos"))))))

(deftest test-futuro-regular
  (testing "La terminación más larga gana: -aremos, no -emos"
    (let [v (:verbo (uno "llegaremos"))]
      (is (= [:venir :futuro 1 :plural] [(:concepto v) (:tiempo v) (:persona v) (:numero v)]))))
  (is (= :futuro (:tiempo (:verbo (uno "visitaran")))))
  (is (= :futuro (:tiempo (:verbo (uno "hablare"))))))

(deftest test-estar
  (is (= [:ser :presente 3 :singular] ((juxt :concepto :tiempo :persona :numero) (:verbo (uno "esta")))))
  (is (= :pasado (:tiempo (:verbo (uno "estuvo"))))))

(deftest test-no-inventa-verbos
  (testing "Una palabra que la BD no conoce queda :desconocida (D-014)"
    (is (= :desconocida (:categoria (uno "armas"))))
    (is (nil? (:verbo (uno "armas"))))))

(deftest test-concepto-de-la-bd
  (testing "llegar es el concepto :venir del manual, también en ir a + infinitivo"
    (is (= :venir (get-in (uno "llegan") [:verbo :concepto])))
    (is (= :venir (get-in (uno "vamos" "a" "llegar") [:verbo :concepto])))))

(deftest test-plural-de-sustantivos
  (is (true? (:plural? (uno "naves"))))
  (is (false? (:plural? (uno "nave"))))
  (is (true? (:plural? (uno "territorios" "reclamados")))))

(deftest test-alternativa-sin-tilde
  (testing "\"envio\" puede ser envío (yo) o envió (él): se anota la otra lectura"
    (is (= {:tiempo :pasado :persona 3 :numero :singular}
           (get-in (uno "envio") [:verbo :alternativa])))))
