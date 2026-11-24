(ns axios.parser
  "Fase 5. Analizador sintáctico descendente recursivo (PP-18, PP-19).
  Responsable: Nicolas (E2).

  Sigue la gramática de la memoria, sección 7.2. Cada no terminal es una
  función que recibe los tokens y la posición, y devuelve [resultado posición].
  Entrega el árbol intermedio de contratos.md (sección 6): solo conceptos
  neutrales, nada de Kethari ni de Ozh.

  Decisiones que aplica: D-006 (sin signos), D-010 (sujeto omitido y \"su\"),
  D-011 (un/una), D-014 (palabras sin equivalente), D-015 y D-023 (verbos con dos
  lecturas), D-017 (pronombres de objeto), D-018 (cuántos), D-020 (sujeto u
  objeto después del verbo) y D-021 (comas y exclamaciones).

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

(def ^:private persona-posesivo
  "Persona y número del poseedor. \"su\" y \"sus\" no están: dependen del tipo de
  oración y se resuelven al final (D-010)."
  {:mi       {:persona 1 :numero :singular}
   :mis      {:persona 1 :numero :singular}
   :tu-pos   {:persona 2 :numero :singular}
   :tus      {:persona 2 :numero :singular}
   :nuestro  {:persona 1 :numero :plural}
   :nuestra  {:persona 1 :numero :plural}
   :nuestros {:persona 1 :numero :plural}
   :nuestras {:persona 1 :numero :plural}})

(def ^:private posesivo-ambiguo #{:su :sus})

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

(def ^:private clitico-ambiguo
  "\"los\" y \"las\" pueden ser ellos o ustedes: se avisa (D-017)."
  #{:los :las})

(def ^:private articulos
  "Determinantes que pueden ir antes del sustantivo. No pasan al árbol (D-011)."
  #{:el-art :la :los :las :un :una})

(def ^:private preposiciones-aceptadas
  "Solo la \"a\" de persona y la \"de\" de posesión (D-014)."
  #{:a :de})

(def ^:private nombre-pronombre
  "Cómo se escribe en español el sujeto deducido, solo para los avisos."
  {[1 :singular] "yo"
   [2 :singular] "usted"
   [3 :singular] "él"
   [1 :plural]   "nosotros"
   [2 :plural]   "ustedes"
   [3 :plural]   "ellos"})

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
  "¿Con este token empieza un sustantivo? (posesivo, número o el sustantivo)."
  [t]
  (or (sustantivo? t) (numero? t) (cat? t :posesivo)))

(defn- articulo?
  "\"el\" llega como pronombre y \"la\", \"los\", \"las\" como determinante: son
  artículo si después viene un sustantivo."
  [ts i]
  (let [t (tok ts i)]
    (and (or (and (cat? t :determinante) (articulos (:concepto t)))
             (and (cat? t :pronombre) (concepto? t :el)))
         (inicia-sustantivo? (tok ts (inc i))))))

(defn- posesivo-tu?
  "\"tu\" es posesivo antes de un sustantivo y pronombre en otro caso (D-003)."
  [ts i]
  (let [t (tok ts i)]
    (and (cat? t :pronombre) (concepto? t :tu)
         (let [s (tok ts (inc i))] (or (sustantivo? s) (numero? s))))))

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

(defn- poseedor-de-posesivo
  "Posesivo antes del sustantivo. \"su/sus\" queda pendiente hasta saber si la
  oración es pregunta o afirmación (D-010)."
  [ts i]
  (let [t (tok ts i)]
    (cond
      (posesivo-tu? ts i)
      {:tipo :pronombre :persona 2 :numero :singular}

      (posesivo-ambiguo (:concepto t))
      {:tipo :pronombre :pendiente-su (:lexema t)}

      :else
      (if-let [p (persona-posesivo (:concepto t))]
        (assoc p :tipo :pronombre)
        (error-estructura "un posesivo conocido" t)))))

(defn- de-poseedor
  "<de-poseedor> ::= \"de\" PRONOMBRE"
  [ts i]
  (let [t (tok ts (inc i))]
    (if (cat? t :pronombre)
      [(dissoc (pronombre-de t) :inferido?) (+ i 2)]
      (error-estructura "un pronombre después de 'de'" t))))

(defn- grupo-sustantivo
  "<grupo-sustantivo> ::= [ <articulo> ] [ POSESIVO ] [ NUMERO ] SUSTANTIVO [ <de-poseedor> ]"
  [ts i]
  (let [i (if (articulo? ts i) (inc i) i)
        [poseedor i] (if (or (cat? (tok ts i) :posesivo) (posesivo-tu? ts i))
                       [(poseedor-de-posesivo ts i) (inc i)]
                       [nil i])
        [cantidad i] (if (numero? (tok ts i))
                       [(:valor (tok ts i)) (inc i)]
                       [nil i])
        s (tok ts i)]
    (when-not (sustantivo? s)
      (error-estructura "un sustantivo" s))
    (let [i (inc i)
          [de i] (if (and (cat? (tok ts i) :preposicion) (concepto? (tok ts i) :de))
                   (de-poseedor ts i)
                   [nil i])]
      (when (and poseedor de)
        (error-estructura "un solo poseedor (posesivo o 'de' + pronombre, no los dos)" (tok ts (- i 2))))
      [{:tipo     :sustantivo
        :concepto (:concepto s)
        :plural?  (boolean (:plural? s))
        :cantidad cantidad
        :poseedor (or poseedor de)
        :lexema   (:lexema s)}
       i])))

(defn- inicia-frase-nominal? [ts i]
  (let [t (tok ts i)]
    (or (and (cat? t :pronombre) (not (posesivo-tu? ts i)))
        (articulo? ts i)
        (posesivo-tu? ts i)
        (inicia-sustantivo? t))))

(defn- frase-nominal
  "<frase-nominal> ::= PRONOMBRE | <grupo-sustantivo>"
  [ts i]
  (let [t (tok ts i)]
    (if (and (cat? t :pronombre) (not (articulo? ts i)) (not (posesivo-tu? ts i)))
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
;; Papeles, sujeto deducido y avisos
;; ---------------------------------------------------------------------------

(defn- concuerda? [frase verbo]
  (= (if (:plural? frase) :plural :singular) (get-in verbo [:verbo :numero])))

(defn- persona-de [frase]
  (case (:tipo frase)
    :pronombre  [(:persona frase) (:numero frase)]
    :sustantivo [3 (if (:plural? frase) :plural :singular)]
    nil))

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

(defn- deducir-sujeto
  "Sujeto no escrito: se deduce del verbo (D-010). En tercera persona, en las
  preguntas se entiende ustedes/usted y en las afirmaciones ellos/él."
  [verbo pregunta?]
  (let [{:keys [persona numero]} (:verbo verbo)
        [persona avisar?] (if (= 3 persona)
                            [(if pregunta? 2 3) true]
                            [persona false])]
    {:sujeto {:tipo :pronombre :persona persona :numero numero :inferido? true}
     :aviso  (when avisar?
               (str "Se interpretó '" (:lexema verbo) "' como '"
                    (nombre-pronombre [persona numero]) " " (:lexema verbo) "' (D-010)."))}))

(defn- resolver-tiempo
  "Verbos con dos lecturas sin tilde (D-023): envio = envío (yo) o envió (él).
  Si el sujeto es de la persona de la otra lectura, se usa esa. Las formas en
  -amos (D-015) tienen la misma persona en las dos lecturas y quedan en presente;
  su aviso lo pone la morfología. Solo se avisa si el sujeto no está escrito:
  con \"yo\" no hay duda."
  [verbo persona-sujeto escrito?]
  (let [{:keys [tiempo persona numero alternativa]} (:verbo verbo)
        alt (when alternativa [(:persona alternativa) (:numero alternativa)])]
    (cond
      (or (nil? alt) (= alt [persona numero]))
      {:tiempo tiempo}

      (= alt persona-sujeto)
      {:tiempo (:tiempo alternativa)}

      :else
      {:tiempo tiempo
       :aviso  (when-not escrito?
                 (str "Se interpretó '" (:lexema verbo) "' en presente, con sujeto 'yo' (D-023)."))})))

(defn- resolver-su
  "\"su/sus\": en preguntas → de ustedes; en afirmaciones → de ellos (D-010)."
  [frase pregunta? avisos]
  (if-let [lex (get-in frase [:poseedor :pendiente-su])]
    (let [persona (if pregunta? 2 3)]
      [(assoc frase :poseedor {:tipo :pronombre :persona persona :numero :plural})
       (conj avisos (str "Se interpretó '" lex "' como 'de "
                         (nombre-pronombre [persona :plural]) "' (D-010)."))])
    [frase avisos]))

(defn- limpiar
  "Quita las marcas internas: el árbol solo lleva lo del contrato."
  [frase]
  (when frase
    (cond-> (dissoc frase :lexema :con-a?)
      (= :sustantivo (:tipo frase)) (update :poseedor #(some-> % (dissoc :inferido?))))))

;; ---------------------------------------------------------------------------
;; Oración
;; ---------------------------------------------------------------------------

(defn- quitar-comas
  "Las comas no cambian el sentido de las oraciones que acepta el sistema (D-021)."
  [tokens]
  (vec (remove #(signo? % ",") tokens)))

(defn- construir-arbol
  [{:keys [tipo interrogativo cuantos pred sin-signos?]}]
  (let [pregunta? (not= :afirmacion tipo)
        verbo     (:verbo pred)
        papeles   (asignar-papeles {:interrogativo interrogativo
                                    :cuantos       cuantos
                                    :sujeto        (:sujeto pred)
                                    :clitico       (:clitico pred)
                                    :complementos  (:complementos pred)
                                    :verbo         verbo})
        avisos    (cond-> []
                    sin-signos? (conj "Se interpretó como afirmación porque no tiene signos de pregunta (D-006)."))
        ;; Sujeto: escrito, la palabra interrogativa (quién) o deducido del verbo.
        [sujeto avisos persona-sujeto]
        (cond
          (:sujeto papeles)
          [(:sujeto papeles) avisos (persona-de (:sujeto papeles))]

          (= :sujeto (:rol interrogativo))
          [nil avisos [3 :singular]]

          (get-in verbo [:verbo :alternativa :persona])
          ;; Sin sujeto y con dos lecturas (D-023 y D-015): se queda el presente.
          [{:tipo :pronombre :persona (get-in verbo [:verbo :persona])
            :numero (get-in verbo [:verbo :numero]) :inferido? true}
           avisos nil]

          :else
          (let [{:keys [sujeto aviso]} (deducir-sujeto verbo pregunta?)]
            [sujeto (cond-> avisos aviso (conj aviso)) (persona-de sujeto)]))
        {:keys [tiempo aviso]} (resolver-tiempo verbo persona-sujeto (some? (:sujeto papeles)))
        avisos (cond-> avisos aviso (conj aviso))
        objeto (:objeto papeles)
        avisos (cond-> avisos
                 (and (:clitico-usado? papeles) (clitico-ambiguo (:concepto (:clitico pred))))
                 (conj (str "Se interpretó '" (:lexema (:clitico pred)) "' como 'ellos' (D-017).")))
        [sujeto avisos] (resolver-su sujeto pregunta? avisos)
        [objeto avisos] (resolver-su objeto pregunta? avisos)]
    {:tipo          tipo
     :interrogativo (cond-> interrogativo
                      (:cuantos-en papeles) (assoc :sobre (:cuantos-en papeles)))
     :sujeto        (limpiar sujeto)
     :verbo         {:concepto (:concepto verbo)
                     :tiempo   tiempo
                     :negado?  (:negado? pred)}
     :objeto        (limpiar objeto)
     :avisos        avisos}))

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
                      :pred          pred
                      :sin-signos?   (and (= :afirmacion tipo) (not exclama?) (nil? cierre))})))

(defn analizar
  "Recibe los tokens de la morfología y devuelve el árbol intermedio
  (contratos.md, sección 6), con :avisos para lo que se interpretó.
  Si no cumplen la gramática, lanza un error de fase :parser."
  [tokens]
  (let [ts (quitar-comas tokens)]
    (when (empty? ts)
      (errores/error-de-fase :parser :estructura-invalida "La oración está vacía." {}))
    (revisar-palabras! ts)
    (oracion ts)))
