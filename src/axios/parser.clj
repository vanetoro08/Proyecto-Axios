(ns axios.parser
  "Fase 5. Analizador sintáctico descendente recursivo (PP-18).
  Responsable: Nicolas (E2).

  Sigue la gramática de la memoria, sección 7.2. Cada no terminal es una
  función que recibe los tokens y la posición, y devuelve [resultado posición].
  Entrega el árbol intermedio de contratos.md (sección 6): solo conceptos
  neutrales, nada de Kethari ni de Ozh.

  Esta primera parte (PP-18) reconoce afirmaciones, negación y preguntas:
  D-006 (sin signos), D-011 (un/una), D-014 (palabras sin equivalente),
  D-017 (pronombres de objeto), D-018 (cuántos), D-020 (sujeto u objeto
  después del verbo) y D-021 (comas y exclamaciones).
  Falta PP-19: posesivos, sujeto omitido (D-010), verbos con dos lecturas
  (D-023) y los avisos.

  Aquí no hay vocabulario: las palabras y sus categorías vienen de la
  morfología, y la lista de verbos sin objeto viene de la BD. Los mapas de
  este archivo están indexados por concepto (las claves del árbol), igual que
  persona-de-pronombre en la morfología."
  (:require [axios.bd :as bd]
            [axios.errores :as errores]))

;; ---------------------------------------------------------------------------
;; Gramática indexada por concepto
;; ---------------------------------------------------------------------------

(def ^:private rol-interrogativo
  "Papel de cada palabra interrogativa (memoria 7.5). \"a quien\" se arma aparte."
  {:cuando  :tiempo
   :donde   :lugar
   :que     :objeto
   :quien   :sujeto
   :cuantos :cantidad})

(def ^:private persona-clitico
  "Pronombres de objeto antes del verbo (D-017): me → yo, te → tú,
  nos → nosotros, lo/la → él, los/las → ellos. \"le\" y \"les\" no están: no
  tienen respaldo en el manual."
  {:me  {:persona 1 :numero :singular}
   :te  {:persona 2 :numero :singular}
   :nos {:persona 1 :numero :plural}
   :lo  {:persona 3 :numero :singular}
   :la  {:persona 3 :numero :singular}
   :los {:persona 3 :numero :plural}
   :las {:persona 3 :numero :plural}})

(def ^:private articulos
  "Determinantes que pueden ir antes del sustantivo. No pasan al árbol (D-011)."
  #{:el-art :la :los :las :un :una})

(def ^:private preposiciones-aceptadas
  "Solo la \"a\" de persona y la \"de\" de posesión (D-014)."
  #{:a :de})

;; ---------------------------------------------------------------------------
;; Ayudas para mirar los tokens
;; ---------------------------------------------------------------------------

(defn- tok [ts i] (get ts i))

(defn- signo? [t s] (and (= :signo (:tipo t)) (= s (:lexema t))))

(defn- cat? [t c] (= c (:categoria t)))

(defn- concepto? [t c] (= c (:concepto t)))

(defn- numero? [t] (= :numero (:tipo t)))

(defn- verbo? [t] (cat? t :verbo))

(defn- sustantivo? [t] (cat? t :sustantivo))

(defn- describir [t]
  (if t (str "'" (:lexema t) "'") "el final de la oración"))

(defn- error-estructura
  "Error de fase :parser con lo que se esperaba y lo que llegó (memoria 7.5)."
  [esperaba t]
  (errores/error-de-fase :parser :estructura-invalida
                         (str "Se esperaba " esperaba " y llegó " (describir t) ".")
                         (cond-> {:esperaba esperaba :encontro (:lexema t)}
                           (:pos t) (assoc :pos (:pos t)))))

;; Palabras que pueden ser de dos categorías (memoria 7.4): basta mirar el
;; token siguiente para decidir.

(defn- inicia-sustantivo?
  "¿Con este token empieza un sustantivo? (número o el sustantivo)."
  [t]
  (or (sustantivo? t) (numero? t)))

(defn- articulo?
  "\"el\" llega como pronombre y \"la\", \"los\", \"las\" como determinante: son
  artículo si después viene un sustantivo."
  [ts i]
  (let [t (tok ts i)]
    (and (or (and (cat? t :determinante) (articulos (:concepto t)))
             (and (cat? t :pronombre) (concepto? t :el)))
         (inicia-sustantivo? (tok ts (inc i))))))

(defn- clitico?
  "Pronombre de objeto: los de la BD (me, te, nos, lo) y \"la\", \"los\", \"las\"
  justo antes del verbo."
  [ts i]
  (let [t (tok ts i)]
    (or (cat? t :clitico)
        (and (cat? t :determinante)
             (persona-clitico (:concepto t))
             (verbo? (tok ts (inc i)))))))

;; ---------------------------------------------------------------------------
;; Revisión previa: palabras que ninguna regla acepta (D-014)
;; ---------------------------------------------------------------------------

(defn- sin-equivalente?
  "Palabras que el sistema conoce en español pero que no tienen traducción ni
  lugar en la gramática."
  [t]
  (case (:categoria t)
    :desconocida   true
    :conjuncion    true
    :preposicion   (not (preposiciones-aceptadas (:concepto t)))
    :interrogativo (not (rol-interrogativo (:concepto t)))
    :clitico       (not (persona-clitico (:concepto t)))
    false))

(defn- revisar-palabras!
  "Si hay palabras sin equivalente, rechaza la oración y las nombra todas."
  [ts]
  (when-let [malas (seq (filter sin-equivalente? ts))]
    (errores/error-de-fase
     :parser :palabra-desconocida
     (apply str (interpose " "
                           (map #(str "'" (:lexema %) "' no tiene equivalente en Kethari ni en Ozh.")
                                malas)))
     {:palabras (mapv #(select-keys % [:lexema :pos]) malas)
      :pos      (:pos (first malas))})))

;; ---------------------------------------------------------------------------
;; Frases nominales
;; ---------------------------------------------------------------------------

(defn- pronombre-de [t]
  (if-let [p (:pronombre t)]
    {:tipo :pronombre :persona (:persona p) :numero (:numero p) :inferido? false}
    (error-estructura "un pronombre conocido" t)))

(defn- grupo-sustantivo
  "<grupo-sustantivo> ::= [ <articulo> ] [ NUMERO ] SUSTANTIVO
  (los posesivos llegan en PP-19)"
  [ts i]
  (let [i (if (articulo? ts i) (inc i) i)
        [cantidad i] (if (numero? (tok ts i))
                       [(:valor (tok ts i)) (inc i)]
                       [nil i])
        s (tok ts i)]
    (when-not (sustantivo? s)
      (error-estructura "un sustantivo" s))
    [{:tipo     :sustantivo
      :concepto (:concepto s)
      :plural?  (boolean (:plural? s))
      :cantidad cantidad
      :poseedor nil
      :lexema   (:lexema s)}
     (inc i)]))

(defn- inicia-frase-nominal? [ts i]
  (let [t (tok ts i)]
    (or (cat? t :pronombre)
        (articulo? ts i)
        (inicia-sustantivo? t))))

(defn- frase-nominal
  "<frase-nominal> ::= PRONOMBRE | <grupo-sustantivo>"
  [ts i]
  (let [t (tok ts i)]
    (if (and (cat? t :pronombre) (not (articulo? ts i)))
      [(assoc (pronombre-de t) :lexema (:lexema t)) (inc i)]
      (grupo-sustantivo ts i))))

;; ---------------------------------------------------------------------------
;; Interrogativo, grupo verbal y complementos
;; ---------------------------------------------------------------------------

(defn- inicia-interrogativo? [ts i]
  (let [t (tok ts i)]
    (or (cat? t :interrogativo)
        (and (cat? t :preposicion) (concepto? t :a)
             (cat? (tok ts (inc i)) :interrogativo)))))

(defn- interrogativo
  "<interrogativo> ::= \"cuando\" | \"donde\" | \"que\" | \"quien\" | \"a\" \"quien\"
                     | <cuantos> SUSTANTIVO"
  [ts i]
  (let [t (tok ts i)]
    (cond
      (cat? t :preposicion)
      (let [q (tok ts (inc i))]
        (if (concepto? q :quien)
          [{:concepto :quien :rol :objeto} nil (+ i 2)]
          (error-estructura "'quien' después de 'a'" q)))

      (concepto? t :cuantos)
      (let [s (tok ts (inc i))]
        (if (sustantivo? s)
          [{:concepto :cuantos :rol :cantidad}
           {:tipo :sustantivo :concepto (:concepto s) :plural? (boolean (:plural? s))
            :cantidad nil :poseedor nil :lexema (:lexema s)}
           (+ i 2)]
          (error-estructura (str "un sustantivo después de '" (:lexema t) "'") s)))

      :else
      [{:concepto (:concepto t) :rol (rol-interrogativo (:concepto t))} nil (inc i)])))

(defn- grupo-verbal
  "<grupo-verbal> ::= [ \"no\" ] [ CLITICO ] VERBO"
  [ts i]
  (let [[negado? i] (if (cat? (tok ts i) :negacion) [true (inc i)] [false i])
        [clitico i] (if (clitico? ts i) [(tok ts i) (inc i)] [nil i])
        v (tok ts i)]
    (when-not (verbo? v)
      (error-estructura "un verbo" v))
    [{:negado? negado? :clitico clitico :verbo v} (inc i)]))

(defn- complemento
  "<complemento> ::= <frase-nominal> | \"a\" PRONOMBRE"
  [ts i]
  (let [t (tok ts i)]
    (if (and (cat? t :preposicion) (concepto? t :a))
      (let [p (tok ts (inc i))]
        (if (cat? p :pronombre)
          [(assoc (pronombre-de p) :lexema (:lexema p) :con-a? true) (+ i 2)]
          (error-estructura "un pronombre después de 'a'" p)))
      (frase-nominal ts i))))

(defn- inicia-complemento? [ts i]
  (let [t (tok ts i)]
    (or (and (cat? t :preposicion) (concepto? t :a))
        (inicia-frase-nominal? ts i))))

(defn- complementos
  "<complementos> ::= ε | <complemento> | <complemento> <complemento>"
  [ts i]
  (loop [i i, out []]
    (if (and (< (count out) 2) (inicia-complemento? ts i))
      (let [[c i] (complemento ts i)] (recur i (conj out c)))
      [out i])))

(defn- predicado
  "<predicado> ::= [ <sujeto> ] <grupo-verbal> <complementos>"
  [ts i]
  (let [[sujeto i] (if (and (inicia-frase-nominal? ts i) (not (clitico? ts i)))
                     (frase-nominal ts i)
                     [nil i])
        [gv i] (grupo-verbal ts i)
        [comps i] (complementos ts i)]
    [(assoc gv :sujeto sujeto :complementos comps) i]))

;; ---------------------------------------------------------------------------
;; Papeles: qué es sujeto y qué es objeto
;; ---------------------------------------------------------------------------

(defn- concuerda? [frase verbo]
  (= (if (:plural? frase) :plural :singular) (get-in verbo [:verbo :numero])))

(defn- asignar-papeles
  "Decide qué es sujeto y qué es objeto (D-020, D-017, D-018).
  Devuelve {:sujeto :objeto :clitico-usado?}."
  [{:keys [interrogativo cuantos sujeto clitico complementos verbo]}]
  (let [sin-objeto? (contains? (bd/verbos-sin-objeto) (get-in verbo [:verbo :infinitivo]))
        estado {:sujeto       sujeto
                :sujeto-ocupado? (or (some? sujeto) (= :sujeto (:rol interrogativo)))
                :objeto       nil
                :objeto-ocupado? (= :objeto (:rol interrogativo))
                :cuantos-en   nil}
        poner-sujeto (fn [e f] (assoc e :sujeto f :sujeto-ocupado? true))
        poner-objeto (fn [e f]
                       (when (:objeto-ocupado? e)
                         (error-estructura "una sola cosa como objeto" {:lexema (:lexema f)}))
                       (assoc e :objeto f :objeto-ocupado? true))
        ;; Sustantivo: sujeto solo si no hay sujeto, el verbo no lleva objeto y
        ;; concuerda en número; si no, objeto (D-020). Si el objeto ya lo ocupa
        ;; la palabra interrogativa, solo puede ser el sujeto.
        poner-sustantivo (fn [e f]
                           (cond
                             (and (not (:sujeto-ocupado? e)) sin-objeto? (concuerda? f verbo))
                             (poner-sujeto e f)

                             (and (:objeto-ocupado? e) (not (:sujeto-ocupado? e)) (concuerda? f verbo))
                             (poner-sujeto e f)

                             :else (poner-objeto e f)))
        e (if cuantos
            (let [e2 (poner-sustantivo estado cuantos)]
              (assoc e2 :cuantos-en (if (identical? (:sujeto e2) cuantos) :sujeto :objeto)))
            estado)
        e (reduce (fn [e c]
                    (cond
                      (:con-a? c)           (poner-objeto e c)
                      (= :pronombre (:tipo c)) (if (:sujeto-ocupado? e) (poner-objeto e c) (poner-sujeto e c))
                      :else                 (poner-sustantivo e c)))
                  e complementos)
        ;; El pronombre de objeto solo cuenta si no hay otro objeto escrito (D-017).
        usar-clitico? (and clitico (not (:objeto-ocupado? e)))
        e (if usar-clitico?
            (assoc e :objeto (assoc (persona-clitico (:concepto clitico))
                                    :tipo :pronombre :inferido? false)
                     :objeto-ocupado? true)
            e)]
    (assoc e :clitico-usado? usar-clitico?)))

(defn- limpiar
  "Quita las marcas internas: el árbol solo lleva lo del contrato."
  [frase]
  (when frase
    (dissoc frase :lexema :con-a?)))

;; ---------------------------------------------------------------------------
;; Oración
;; ---------------------------------------------------------------------------

(defn- quitar-comas
  "Las comas no cambian el sentido de las oraciones que acepta el sistema (D-021)."
  [tokens]
  (vec (remove #(signo? % ",") tokens)))

(defn- construir-arbol
  "Arma el árbol del contrato. Si el sujeto no está escrito, queda en nil:
  deducirlo del verbo es parte de PP-19 (D-010)."
  [{:keys [tipo interrogativo cuantos pred]}]
  (let [verbo   (:verbo pred)
        papeles (asignar-papeles {:interrogativo interrogativo
                                  :cuantos       cuantos
                                  :sujeto        (:sujeto pred)
                                  :clitico       (:clitico pred)
                                  :complementos  (:complementos pred)
                                  :verbo         verbo})]
    {:tipo          tipo
     :interrogativo (cond-> interrogativo
                      (:cuantos-en papeles) (assoc :sobre (:cuantos-en papeles)))
     :sujeto        (limpiar (:sujeto papeles))
     :verbo         {:concepto (:concepto verbo)
                     :tiempo   (get-in verbo [:verbo :tiempo])
                     :negado?  (:negado? pred)}
     :objeto        (limpiar (:objeto papeles))}))

(defn- oracion
  "<oracion> ::= <pregunta-abierta> | <pregunta-si-no> | <afirmacion>
   <pregunta-abierta> ::= <abre> <interrogativo> <predicado> <cierra>
   <pregunta-si-no>   ::= \"¿\" <predicado> <cierra> | <predicado> \"?\"
   <afirmacion>       ::= [ \"¡\" ] <predicado> <final>"
  [ts]
  (let [exclama? (signo? (tok ts 0) "¡")
        abre?    (signo? (tok ts 0) "¿")
        i        (if (or exclama? abre?) 1 0)
        abierta? (and (not exclama?) (inicia-interrogativo? ts i))
        [interrogativo cuantos i] (if abierta? (interrogativo ts i) [nil nil i])
        [pred i] (predicado ts i)
        t        (tok ts i)
        [cierre i] (cond
                     (signo? t "?") ["?" (inc i)]
                     (signo? t ".") ["." (inc i)]
                     (signo? t "!") ["!" (inc i)]
                     :else          [nil i])
        tipo     (cond
                   exclama?                    (if (= "?" cierre)
                                                 (error-estructura "'!' o '.' al final de una oración con '¡'" t)
                                                 :afirmacion)
                   abierta?                    (if (#{"." "!"} cierre)
                                                 (error-estructura "'?' al final de la pregunta" t)
                                                 :pregunta-abierta)
                   (or abre? (= "?" cierre))   (if (#{"." "!"} cierre)
                                                 (error-estructura "'?' al final de la pregunta" t)
                                                 :pregunta-si-no)
                   :else                       :afirmacion)]
    (when (< i (count ts))
      (error-estructura (if cierre "el final de la oración" "el final de la oración o un signo de cierre")
                        (tok ts i)))
    (construir-arbol {:tipo          tipo
                      :interrogativo interrogativo
                      :cuantos       cuantos
                      :pred          pred})))

(defn analizar
  "Recibe los tokens de la morfología y devuelve el árbol intermedio
  (contratos.md, sección 6).
  Si no cumplen la gramática, lanza un error de fase :parser."
  [tokens]
  (let [ts (quitar-comas tokens)]
    (when (empty? ts)
      (errores/error-de-fase :parser :estructura-invalida "La oración está vacía." {}))
    (revisar-palabras! ts)
    (oracion ts)))
