(ns axios.parser-test
  "Pruebas del parser, primera parte (PP-18): afirmaciones, negación y preguntas.
  Responsable: Nicolas.
  Cada oración pasa por las fases reales (normalizador → lexer → corrector →
  morfología) antes del parser, como en el pipeline."
  (:require [axios.bd :as bd]
            [axios.corrector :as corrector]
            [axios.lexer :as lexer]
            [axios.morfologia :as morfologia]
            [axios.normalizador :as normalizador]
            [axios.parser :as parser]
            [clojure.test :refer [deftest is testing]]))

(defn- arbol [texto]
  (-> (normalizador/normalizar texto)
      :texto
      lexer/tokenizar
      (corrector/corregir (bd/vocabulario-es))
      :tokens
      morfologia/analizar
      parser/analizar))

(defn- error [texto]
  (try (arbol texto) nil
       (catch clojure.lang.ExceptionInfo e
         (assoc (ex-data e) :mensaje (ex-message e)))))

(def ^:private ustedes {:tipo :pronombre :persona 2 :numero :plural :inferido? false})
(def ^:private nosotros {:tipo :pronombre :persona 1 :numero :plural :inferido? false})

;; ---------------------------------------------------------------------------
;; Contrato del árbol (contratos.md, sección 6)
;; ---------------------------------------------------------------------------

(deftest arbol-del-contrato
  (testing "¿Cuándo vendrán ustedes? da exactamente el árbol de ejemplo del contrato"
    (is (= {:tipo          :pregunta-abierta
            :interrogativo {:concepto :cuando :rol :tiempo}
            :sujeto        ustedes
            :verbo         {:concepto :venir :tiempo :futuro :negado? false}
            :objeto        nil}
           (arbol "¿Cuándo vendrán ustedes?")))))

;; ---------------------------------------------------------------------------
;; Oraciones de las señales (memoria 7.8)
;; ---------------------------------------------------------------------------

(deftest oraciones-de-las-senales
  (testing "día 9: '¿A quién escuchan ustedes?' → interrogativo con rol de objeto"
    (let [a (arbol "¿A quién escuchan ustedes?")]
      (is (= {:concepto :quien :rol :objeto} (:interrogativo a)))
      (is (= ustedes (:sujeto a)))
      (is (nil? (:objeto a)))))
  (testing "día 6: '¿Quién envió el mensaje?' → quién es el sujeto y 'el mensaje' el objeto"
    (let [a (arbol "¿Quién envió el mensaje?")]
      (is (= {:concepto :quien :rol :sujeto} (:interrogativo a)))
      (is (nil? (:sujeto a)))
      (is (= :mensaje (get-in a [:objeto :concepto])))))
  (testing "día 9: '¿Es él una criatura?' → 'el' es pronombre, 'una' es artículo (D-011)"
    (let [a (arbol "¿Es él una criatura?")]
      (is (= :pregunta-si-no (:tipo a)))
      (is (= {:tipo :pronombre :persona 3 :numero :singular :inferido? false} (:sujeto a)))
      (is (= {:tipo :sustantivo :concepto :criatura :plural? false :cantidad nil :poseedor nil}
             (:objeto a)))))
  (testing "día 2: 'Nosotros queremos paz.'"
    (let [a (arbol "Nosotros queremos paz.")]
      (is (= :afirmacion (:tipo a)))
      (is (= nosotros (:sujeto a)))
      (is (= :paz (get-in a [:objeto :concepto])))))
  (testing "día 4: 'Nosotros no los vimos a ustedes.' → un solo objeto: ustedes (D-017)"
    (let [a (arbol "Nosotros no los vimos a ustedes.")]
      (is (true? (get-in a [:verbo :negado?])))
      (is (= :pasado (get-in a [:verbo :tiempo])))
      (is (= ustedes (:objeto a)))))
  (testing "día 3: 'Quince naves llegaron' → número antes del sustantivo"
    (is (= {:tipo :sustantivo :concepto :nave :plural? true :cantidad 15 :poseedor nil}
           (:sujeto (arbol "Quince naves llegaron."))))))

;; ---------------------------------------------------------------------------
;; Decisiones de la bitácora
;; ---------------------------------------------------------------------------

(deftest signos-d006
  (testing "sin signos ni interrogativa → afirmación"
    (is (= :afirmacion (:tipo (arbol "vienen ustedes")))))
  (testing "con un solo signo ya es pregunta"
    (is (= :pregunta-si-no (:tipo (arbol "vienen ustedes?"))))
    (is (= :pregunta-si-no (:tipo (arbol "¿vienen ustedes")))))
  (testing "sin signos pero con interrogativa al inicio → pregunta abierta"
    (is (= :pregunta-abierta (:tipo (arbol "cuando vienen ustedes"))))))

(deftest negacion
  (is (true? (get-in (arbol "Ustedes no hablan") [:verbo :negado?])))
  (is (true? (get-in (arbol "¿No vienen ustedes?") [:verbo :negado?])))
  (is (false? (get-in (arbol "Ustedes hablan") [:verbo :negado?]))))

(deftest un-una-d011
  (is (nil? (get-in (arbol "¿Quieren ustedes una nave?") [:objeto :cantidad])))
  (is (= 1 (get-in (arbol "¿Quieren ustedes 1 nave?") [:objeto :cantidad]))))

(deftest palabras-sin-equivalente-d014
  (testing "se nombra la palabra y no se inventa nada"
    (let [e (error "¿Están en su planeta?")]
      (is (= :parser (:fase e)))
      (is (= :palabra-desconocida (:tipo e)))
      (is (= "'en' no tiene equivalente en Kethari ni en Ozh." (:mensaje e)))))
  (is (= :palabra-desconocida (:tipo (error "¿Cuál es su planeta?"))))
  (is (= :palabra-desconocida (:tipo (error "Queremos paz y agua."))))
  (testing "se nombran todas las palabras desconocidas, no solo la primera"
    (is (= ["traen" "armas"] (map :lexema (get-in (error "¿Traen armas?") [:detalle :palabras]))))))

(deftest pronombres-de-objeto-d017
  (testing "'No los vimos' → objeto ellos"
    (is (= {:tipo :pronombre :persona 3 :numero :plural :inferido? false}
           (:objeto (arbol "No los vimos.")))))
  (testing "'¿Nos escuchan?' → objeto nosotros (el corrector no cambia 'nos')"
    (is (= {:tipo :pronombre :persona 1 :numero :plural :inferido? false}
           (:objeto (arbol "¿Nos escuchan?")))))
  (testing "'le' y 'les' no tienen respaldo en el manual"
    (is (= :palabra-desconocida (:tipo (error "¿Les escuchan?"))))))

(deftest cuantos-d018
  (testing "con verbo sin objeto, el sustantivo es el sujeto"
    (let [a (arbol "¿Cuántas naves llegaron?")]
      (is (= {:concepto :cuantos :rol :cantidad :sobre :sujeto} (:interrogativo a)))
      (is (= :nave (get-in a [:sujeto :concepto])))))
  (testing "con otro verbo, el sustantivo es el objeto"
    (let [a (arbol "¿Cuántas naves quieren ustedes?")]
      (is (= :objeto (get-in a [:interrogativo :sobre])))
      (is (= ustedes (:sujeto a)))
      (is (= :nave (get-in a [:objeto :concepto]))))))

(deftest sujeto-u-objeto-despues-del-verbo-d020
  (testing "llegar no lleva objeto: 'quince naves' es el sujeto"
    (let [a (arbol "¿Cuándo llegan quince naves?")]
      (is (= {:tipo :sustantivo :concepto :nave :plural? true :cantidad 15 :poseedor nil}
             (:sujeto a)))
      (is (nil? (:objeto a)))))
  (testing "querer sí lleva objeto: 'una nave' es el objeto"
    (is (= :nave (get-in (arbol "¿Quieren ustedes una nave?") [:objeto :concepto]))))
  (testing "estar no lleva objeto: 'el planeta' es el sujeto"
    (is (= :planeta (get-in (arbol "¿Dónde está el planeta?") [:sujeto :concepto]))))
  (testing "pronombre después del verbo: sujeto"
    (is (= ustedes (:sujeto (arbol "¿Ven ustedes la nave?")))))
  (testing "si 'qué' ya es el objeto, el sustantivo es el sujeto"
    (is (= :criatura (get-in (arbol "¿Qué ven las criaturas?") [:sujeto :concepto])))))

(deftest comas-y-exclamaciones-d021
  (is (= (arbol "Nosotros queremos paz.")
         (arbol "Nosotros, queremos paz.")
         (arbol "¡Nosotros queremos paz!")))
  (is (= :afirmacion (:tipo (arbol "¡Queremos paz!")))))

(deftest palabras-con-dos-categorias
  (testing "'el' antes de sustantivo es artículo y antes de verbo es pronombre"
    (is (= :mensaje (get-in (arbol "Él no escucha el mensaje") [:objeto :concepto])))
    (is (= {:tipo :pronombre :persona 3 :numero :singular :inferido? false}
           (:sujeto (arbol "Él no escucha el mensaje")))))
  (testing "'los' antes de sustantivo es artículo y antes de verbo es pronombre de objeto"
    (is (= :planeta (get-in (arbol "Ellos visitaron los planetas.") [:objeto :concepto])))))

(deftest perifrasis-y-numeros
  (is (= {:concepto :esperar :tiempo :futuro :negado? true}
         (:verbo (arbol "Yo no voy a esperar"))))
  (is (= 2015 (get-in (arbol "Nosotros esperaremos dos mil quince ciclos") [:objeto :cantidad])))
  (is (= 9999 (get-in (arbol "Nosotros vimos 9999 estrellas") [:objeto :cantidad]))))

;; ---------------------------------------------------------------------------
;; Oraciones que no cumplen la gramática (memoria 7.8)
;; ---------------------------------------------------------------------------

(deftest estructuras-invalidas
  (testing "falta el verbo"
    (let [e (error "¿Cuándo las naves?")]
      (is (= :estructura-invalida (:tipo e)))
      (is (= "Se esperaba un verbo y llegó '?'." (:mensaje e)))
      (is (map? (get-in e [:detalle :pos])))))
  (testing "después del verbo no puede venir otro verbo"
    (is (= :estructura-invalida (:tipo (error "¿Llegan llegan?")))))
  (testing "una pregunta que abre con ¿ no puede cerrar con punto"
    (is (= :estructura-invalida (:tipo (error "¿Vienen ustedes.")))))
  (testing "una exclamación no puede cerrar con ?"
    (is (= :estructura-invalida (:tipo (error "¡Vienen ustedes?"))))))
