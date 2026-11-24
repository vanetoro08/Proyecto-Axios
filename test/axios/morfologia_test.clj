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