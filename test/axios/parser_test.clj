(ns axios.parser-test
  "Pruebas del parser (PP-18, PP-19). Responsable: Nicolas.
  Cada oración pasa por las fases reales (normalizador → lexer → corrector →
  morfología) antes del parser, como en el pipeline."
  (:require [axios.bd :as bd]
            [axios.corrector :as corrector]
            [axios.lexer :as lexer]
            [axios.morfologia :as morfologia]
            [axios.normalizador :as normalizador]
            [axios.parser :as parser]
            [axios.pipeline :as pipeline]
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
            :objeto        nil
            :avisos        []}
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
  (testing "día 6: '¿Quién envió el mensaje?' → quién es el sujeto, el verbo en pasado (D-023)"
    (let [a (arbol "¿Quién envió el mensaje?")]
      (is (= {:concepto :quien :rol :sujeto} (:interrogativo a)))
      (is (nil? (:sujeto a)))
      (is (= :pasado (get-in a [:verbo :tiempo])))
      (is (= :mensaje (get-in a [:objeto :concepto])))
      (is (empty? (:avisos a)))))
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
      (is (= ustedes (:objeto a)))
      (is (empty? (:avisos a)))))
  (testing "día 3: 'Quince naves llegaron' → número antes del sustantivo"
    (is (= {:tipo :sustantivo :concepto :nave :plural? true :cantidad 15 :poseedor nil}
           (:sujeto (arbol "Quince naves llegaron."))))))

;; ---------------------------------------------------------------------------
;; Decisiones de la bitácora
;; ---------------------------------------------------------------------------

(deftest signos-d006
  (testing "sin signos ni interrogativa → afirmación con aviso"
    (let [a (arbol "vienen ustedes")]
      (is (= :afirmacion (:tipo a)))
      (is (some #(.contains ^String % "D-006") (:avisos a)))))
  (testing "con un solo signo ya es pregunta"
    (is (= :pregunta-si-no (:tipo (arbol "vienen ustedes?"))))
    (is (= :pregunta-si-no (:tipo (arbol "¿vienen ustedes")))))
  (testing "sin signos pero con interrogativa al inicio → pregunta abierta"
    (is (= :pregunta-abierta (:tipo (arbol "cuando vienen ustedes"))))))

(deftest sujeto-omitido-y-su-d010
  (testing "pregunta en 3.ª plural → ustedes, con aviso"
    (let [a (arbol "¿Cuándo llegan?")]
      (is (= {:tipo :pronombre :persona 2 :numero :plural :inferido? true} (:sujeto a)))
      (is (= ["Se interpretó 'llegan' como 'ustedes llegan' (D-010)."] (:avisos a)))))
  (testing "afirmación en 3.ª plural → ellos"
    (is (= {:tipo :pronombre :persona 3 :numero :plural :inferido? true}
           (:sujeto (arbol "Quieren paz.")))))
  (testing "1.ª persona → nosotros, sin aviso"
    (let [a (arbol "¿Cuándo llegamos?")]
      (is (= {:tipo :pronombre :persona 1 :numero :plural :inferido? true} (:sujeto a)))
      (is (empty? (:avisos a)))))
  (testing "'su' en pregunta → de ustedes, con aviso"
    (let [a (arbol "¿Dónde está su planeta?")]
      (is (= {:tipo :pronombre :persona 2 :numero :plural} (get-in a [:sujeto :poseedor])))
      (is (= ["Se interpretó 'su' como 'de ustedes' (D-010)."] (:avisos a)))))
  (testing "'sus' en afirmación → de ellos"
    (is (= {:tipo :pronombre :persona 3 :numero :plural}
           (get-in (arbol "Vimos sus naves.") [:objeto :poseedor])))))

(deftest un-una-d011
  (is (nil? (get-in (arbol "¿Quieren una nave?") [:objeto :cantidad])))
  (is (= 1 (get-in (arbol "¿Quieren 1 nave?") [:objeto :cantidad]))))

(deftest palabras-sin-equivalente-d014
  (testing "se nombra la palabra y no se inventa nada"
    (let [e (error "¿Están en su planeta?")]
      (is (= :parser (:fase e)))
      (is (= :palabra-desconocida (:tipo e)))
      (is (= "'en' no tiene equivalente en Kethari ni en Ozh." (:mensaje e)))))
  (is (= :palabra-desconocida (:tipo (error "¿Cuál es su planeta?"))))
  (is (= :palabra-desconocida (:tipo (error "Queremos paz y agua."))))
  (testing "se nombran todas las palabras desconocidas, no solo la primera"
    (is (= ["traen" "armas"] (map :lexema (get-in (error "¿Traen armas?") [:detalle :palabras])))))
  (testing "la 'a' de persona y la 'de' de posesión sí se aceptan"
    (is (= {:tipo :pronombre :persona 2 :numero :plural}
           (get-in (arbol "Ellos visitaron el planeta de ustedes.") [:objeto :poseedor])))))

(deftest verbos-con-dos-lecturas-d015-d023
  (testing "-amos queda en presente (D-015)"
    (is (= :presente (get-in (arbol "Nosotros enviamos mensajes") [:verbo :tiempo]))))
  (testing "envio con sujeto 'yo' → presente, sin aviso"
    (let [a (arbol "Yo envío mensajes.")]
      (is (= :presente (get-in a [:verbo :tiempo])))
      (is (empty? (:avisos a)))))
  (testing "envio sin sujeto → presente de 'yo', con aviso"
    (let [a (arbol "envio mensajes.")]
      (is (= {:tipo :pronombre :persona 1 :numero :singular :inferido? true} (:sujeto a)))
      (is (= ["Se interpretó 'envio' en presente, con sujeto 'yo' (D-023)."] (:avisos a)))))
  (testing "con sujeto de 3.ª singular se usa la otra lectura: pasado"
    (is (= :pasado (get-in (arbol "¿Llegó la nave?") [:verbo :tiempo])))
    (is (= :pasado (get-in (arbol "Él envió el mensaje.") [:verbo :tiempo])))))

(deftest pronombres-de-objeto-d017
  (testing "'No los vimos' → objeto ellos, con aviso"
    (let [a (arbol "No los vimos.")]
      (is (= {:tipo :pronombre :persona 3 :numero :plural :inferido? false} (:objeto a)))
      (is (= ["Se interpretó 'los' como 'ellos' (D-017)."] (:avisos a)))))
  (testing "'¿Nos escuchan?' → objeto nosotros, sujeto ustedes (el corrector no cambia 'nos')"
    (let [a (arbol "¿Nos escuchan?")]
      (is (= {:tipo :pronombre :persona 1 :numero :plural :inferido? false} (:objeto a)))
      (is (= 2 (get-in a [:sujeto :persona])))))
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
  (testing "llegar no lleva objeto: 'sus naves' es el sujeto"
    (let [a (arbol "¿Cuándo llegan sus quince naves?")]
      (is (= {:tipo :sustantivo :concepto :nave :plural? true :cantidad 15
              :poseedor {:tipo :pronombre :persona 2 :numero :plural}}
             (:sujeto a)))
      (is (nil? (:objeto a)))))
  (testing "querer sí lleva objeto: 'una nave' es el objeto"
    (is (= :nave (get-in (arbol "¿Quieren una nave?") [:objeto :concepto]))))
  (testing "estar no lleva objeto: 'su planeta' es el sujeto"
    (is (= :planeta (get-in (arbol "¿Dónde está su planeta?") [:sujeto :concepto]))))
  (testing "pronombre después del verbo: sujeto"
    (is (= ustedes (:sujeto (arbol "¿Ven ustedes la nave?"))))))

(deftest comas-y-exclamaciones-d021
  (is (= (dissoc (arbol "Nosotros queremos paz.") :avisos)
         (dissoc (arbol "Nosotros, queremos paz.") :avisos)
         (dissoc (arbol "¡Nosotros queremos paz!") :avisos)))
  (is (= :afirmacion (:tipo (arbol "¡Queremos paz!")))))

(deftest palabras-con-dos-categorias
  (testing "'el' antes de sustantivo es artículo"
    (is (= :mensaje (get-in (arbol "Él no escucha el mensaje") [:objeto :concepto]))))
  (testing "'tu' antes de sustantivo es posesivo y antes de verbo es pronombre"
    (is (= {:tipo :pronombre :persona 2 :numero :singular}
           (get-in (arbol "Visitaremos tu planeta") [:objeto :poseedor])))
    (is (= {:tipo :pronombre :persona 2 :numero :singular :inferido? false}
           (:sujeto (arbol "tu vienes."))))))

(deftest perifrasis-y-numeros
  (is (= {:concepto :esperar :tiempo :futuro :negado? true}
         (:verbo (arbol "Yo no voy a esperar"))))
  (is (= 2015 (get-in (arbol "Esperaremos dos mil quince ciclos") [:objeto :cantidad])))
  (is (= 9999 (get-in (arbol "Vimos 9999 estrellas") [:objeto :cantidad]))))

;; ---------------------------------------------------------------------------
;; Oraciones que no cumplen la gramática (memoria 7.8)
;; ---------------------------------------------------------------------------

(deftest estructuras-invalidas
  (testing "falta el verbo"
    (let [e (error "¿Cuándo sus naves?")]
      (is (= :estructura-invalida (:tipo e)))
      (is (= "Se esperaba un verbo y llegó '?'." (:mensaje e)))
      (is (map? (get-in e [:detalle :pos])))))
  (testing "después del verbo no puede venir otro verbo"
    (is (= :estructura-invalida (:tipo (error "¿Llegan llegan?")))))
  (testing "una pregunta que abre con ¿ no puede cerrar con punto"
    (is (= :estructura-invalida (:tipo (error "¿Vienen ustedes.")))))
  (testing "una exclamación no puede cerrar con ?"
    (is (= :estructura-invalida (:tipo (error "¡Vienen ustedes?"))))))

;; ---------------------------------------------------------------------------
;; Avisos en el resultado del pipeline (contratos.md, sección 9)
;; ---------------------------------------------------------------------------

(deftest el-pipeline-junta-los-avisos
  (is (= ["Se interpretó 'llegan' como 'ustedes llegan' (D-010)."]
         (:avisos (pipeline/traducir "¿Cuándo llegan?"))))
  (testing "el aviso de la morfología (D-015) también llega"
    (is (some #(.contains ^String % "D-015")
              (:avisos (pipeline/traducir "Nosotros enviamos mensajes."))))))
