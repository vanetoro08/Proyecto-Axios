(ns axios.errores-test
  "Pruebas automáticas de PP-27 (palabras desconocidas) y PP-28 (manejo de errores)."
  (:require [axios.errores :as errores]
            [axios.validador.kethari :as vk]
            [axios.validador.ozh :as vo]
            [clojure.test :refer [deftest is testing]]))

(deftest error-de-fase-cumple-contrato-seccion-8
  (let [ex (try
             (errores/error-de-fase :parser :estructura-invalida "Falta el verbo" {:pos 12})
             (catch clojure.lang.ExceptionInfo e e))]
    (is (some? ex))
    (is (= "Falta el verbo" (.getMessage ex)))
    (is (= {:fase :parser :tipo :estructura-invalida :detalle {:pos 12}}
           (ex-data ex)))))

(deftest extraer-error-maneja-ex-info-y-excepciones-inesperadas
  (let [e1 (try (errores/error-de-fase :lexer :caracter-invalido "Símbolo '@'" {:pos 3})
                (catch Throwable e (errores/extraer-error e)))
        e2 (errores/extraer-error (RuntimeException. "Fallo interno"))]
    (is (= :lexer (:fase e1)))
    (is (= :caracter-invalido (:tipo e1)))
    (is (= :pipeline (:fase e2)))
    (is (= :error-interno (:tipo e2)))))

(deftest pp27-detecta-y-retiene-palabras-desconocidas
  (testing "Detecta tokens con :categoria :desconocida (D-014)"
    (let [toks [{:tipo :palabra :lexema "estan" :categoria :verbo}
                {:tipo :palabra :lexema "en" :categoria :desconocida :pos {:inicio 6 :fin 8}}
                {:tipo :palabra :lexema "su" :categoria :posesivo}
                {:tipo :palabra :lexema "planeta" :categoria :sustantivo}]
          desc (errores/detectar-desconocidas toks)]
      (is (= 1 (count desc)))
      (is (= "en" (:lexema (first desc))))))

  (testing "verificar-desconocidas! lanza error-de-fase :palabra-desconocida"
    (let [ex (try
               (errores/verificar-desconocidas!
                 [{:tipo :palabra :lexema "armas" :categoria :desconocida :pos {:inicio 7 :fin 12}}])
               (catch clojure.lang.ExceptionInfo e e))]
      (is (= :morfologia (:fase (ex-data ex))))
      (is (= :palabra-desconocida (:tipo (ex-data ex))))
      (is (.contains ^String (.getMessage ex) "armas"))))

  (testing "D-005: validar-concepto-en-lengua! explica ausencia de 'tierra' en Kethari"
    (let [ex (try
               (errores/validar-concepto-en-lengua! :tierra :kethari nil)
               (catch clojure.lang.ExceptionInfo e e))]
      (is (= :generador (:fase (ex-data ex))))
      (is (= :palabra-desconocida (:tipo (ex-data ex))))
      (is (.contains ^String (.getMessage ex) "manual 0.8")))))

(deftest pp28-explicaciones-claras-por-fase-y-por-validadores-d009
  (let [msg-desc (errores/explicar {:fase :morfologia :tipo :palabra-desconocida}
                                   "'en' no tiene equivalente.")
        msg-lex  (errores/explicar {:fase :lexer :tipo :caracter-invalido :detalle {:pos {:inicio 4 :fin 5}}}
                                   "Carácter '@' no permitido.")
        msg-val  (errores/explicar-rechazo-validadores
                   (vk/validar "anul parn thal")
                   (vo/validar "lom ʔenka ilu"))]
    (is (.contains ^String msg-desc "inventar"))
    (is (.contains ^String msg-lex "4–5"))
    (is (.contains ^String msg-val "D-009"))
    (is (.contains ^String msg-val "partícula"))))
