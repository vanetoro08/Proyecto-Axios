(ns axios.lexer-test
  (:require [clojure.test :refer [deftest testing is]]
            [axios.lexer :as lexer]))

(deftest test-tokenizar-basico
  (testing "Tokeniza una pregunta normal"
    (is (= [{:tipo :signo :lexema "¿" :pos {:inicio 0 :fin 1}}
            {:tipo :palabra :lexema "cuando" :pos {:inicio 1 :fin 7}}
            {:tipo :palabra :lexema "vendran" :pos {:inicio 8 :fin 15}}
            {:tipo :palabra :lexema "ustedes" :pos {:inicio 16 :fin 23}}
            {:tipo :signo :lexema "?" :pos {:inicio 23 :fin 24}}]
           (lexer/tokenizar "¿cuando vendran ustedes?")))))

(deftest test-tokenizar-numeros
  (testing "Tokeniza números y palabras mezcladas"
    (is (= [{:tipo :palabra :lexema "quince" :pos {:inicio 0 :fin 6}}
            {:tipo :palabra :lexema "naves" :pos {:inicio 7 :fin 12}}
            {:tipo :palabra :lexema "llegaron" :pos {:inicio 13 :fin 21}}]
           (lexer/tokenizar "quince naves llegaron")))
    (is (= [{:tipo :numero :lexema "15" :valor 15 :pos {:inicio 0 :fin 2}}
            {:tipo :palabra :lexema "naves" :pos {:inicio 3 :fin 8}}
            {:tipo :palabra :lexema "llegaron" :pos {:inicio 9 :fin 17}}]
           (lexer/tokenizar "15 naves llegaron")))))

(deftest test-caracter-invalido
  (testing "Lanza excepción con caracteres no reconocidos"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Carácter inválido"
                          (lexer/tokenizar "hola @ mundo")))))