(ns axios.validador.kethari
  "Validador de Kethari: analizador léxico y sintáctico propio, independiente
  del generador (contratos.md, sección 7). Condición 2 de Axios.
  Responsable: Jhojan (PP-25).

  Recibe SOLO el texto y lo vuelve a leer mediante un AFD manual y un parser
  descendente recursivo. Todo el léxico y las reglas se consultan de la BD
  (cumple D-004, D-007, D-013 y D-019)."
  (:require [axios.bd :as bd]
            [axios.validador.lexer :as lexer]))

(def ^:private fase :validador-kethari)

(defn- err [tipo mensaje]
  {:fase fase :tipo tipo :mensaje mensaje})

;; --- Análisis morfológico dirigido por reglas de BD ---

(defn- analizar-numero
  [lexema formas reglas]
  (let [digitos (:digito formas)
        sep-str (lexer/obtener-regla reglas :numeros.separador)
        sep (when (seq sep-str) (first sep-str))
        base (lexer/parse-entero (lexer/obtener-regla reglas :numeros.base) 6)
        maximo 9999
        partes (if sep (lexer/dividir-por-caracter lexema sep) [lexema])
        tiene-sep? (> (count partes) 1)]
    (cond
      (and (seq digitos) (every? #(contains? digitos %) partes))
      (let [valores (mapv #(get digitos %) partes)
            valor (reduce (fn [acc d] (+ (* acc base) d)) 0 valores)]
        (cond
          (and tiene-sep? (zero? (first valores)))
          {:error (err :numero-invalido
                       (str "Número Kethari inválido '" lexema "': no se permiten ceros a la izquierda."))}

          (> valor maximo)
          {:error (err :numero-fuera-de-rango
                       (str "Número Kethari '" lexema "' (" valor ") supera el máximo permitido (" maximo ")."))}

          :else
          {:categoria :numero :valor valor :base base}))

      tiene-sep?
      {:error (err :numero-invalido
                   (str "Número Kethari inválido '" lexema "': contiene cifras no válidas en base " base "."))}

      :else nil)))

(defn- analizar-verbo
  [lexema formas reglas]
  (let [verbos (:verbo formas)
        pref-neg (lexer/obtener-regla reglas :prefijo.negacion)
        suf-pas (lexer/obtener-regla reglas :sufijo.pasado)
        suf-fut (lexer/obtener-regla reglas :sufijo.futuro)]
    (letfn [(extraer-tiempo [forma]
              (cond
                (contains? verbos forma)
                {:raiz forma :tiempo :presente}

                (and (lexer/termina-con? forma suf-pas)
                     (contains? verbos (lexer/quitar-sufijo forma suf-pas)))
                {:raiz (lexer/quitar-sufijo forma suf-pas) :tiempo :pasado}

                (and (lexer/termina-con? forma suf-fut)
                     (contains? verbos (lexer/quitar-sufijo forma suf-fut)))
                {:raiz (lexer/quitar-sufijo forma suf-fut) :tiempo :futuro}

                :else nil))]
      (or (when-let [v (extraer-tiempo lexema)]
            (assoc v :categoria :verbo :negado? false))
          (when (lexer/empieza-con? lexema pref-neg)
            (when-let [v (extraer-tiempo (lexer/quitar-prefijo lexema pref-neg))]
              (assoc v :categoria :verbo :negado? true)))))))

(defn- analizar-sustantivo
  [lexema formas reglas]
  (let [susts (:sustantivo formas)
        suf-pl (lexer/obtener-regla reglas :sufijo.plural)]
    (cond
      (contains? susts lexema)
      {:categoria :sustantivo :raiz lexema :plural? false}

      (and (lexer/termina-con? lexema suf-pl)
           (contains? susts (lexer/quitar-sufijo lexema suf-pl)))
      {:categoria :sustantivo :raiz (lexer/quitar-sufijo lexema suf-pl) :plural? true}

      :else nil)))

(defn- diagnosticar-morfologia-invalida
  [lexema formas reglas]
  (let [verbos (:verbo formas)
        susts (:sustantivo formas)
        pref-neg (lexer/obtener-regla reglas :prefijo.negacion)
        suf-pas (lexer/obtener-regla reglas :sufijo.pasado)
        suf-fut (lexer/obtener-regla reglas :sufijo.futuro)
        suf-pl (lexer/obtener-regla reglas :sufijo.plural)
        sin-neg (if (lexer/empieza-con? lexema pref-neg)
                  (lexer/quitar-prefijo lexema pref-neg)
                  lexema)]
    (cond
      (and (lexer/empieza-con? lexema pref-neg) (lexer/empieza-con? sin-neg pref-neg))
      (err :morfologia-invalida
           (str "Verbo Kethari inválido '" lexema "': prefijo de negación '" pref-neg "' duplicado."))

      (some (fn [doble]
              (and (lexer/termina-con? sin-neg doble)
                   (contains? verbos (lexer/quitar-sufijo sin-neg doble))))
            [(str suf-pas suf-fut) (str suf-fut suf-pas)
             (str suf-pas suf-pas) (str suf-fut suf-fut)])
      (err :morfologia-invalida
           (str "Verbo Kethari inválido '" lexema "': combina múltiples sufijos de tiempo."))

      (and (lexer/termina-con? lexema (str suf-pl suf-pl))
           (contains? susts (lexer/quitar-sufijo lexema (str suf-pl suf-pl))))
      (err :morfologia-invalida
           (str "Sustantivo Kethari inválido '" lexema "': sufijo plural '" suf-pl "' duplicado."))

      :else nil)))

(defn- clasificar-token
  [tok formas reglas]
  (let [{:keys [lexema]} tok
        part-preg (lexer/obtener-regla reglas :particula.pregunta)
        interrogs (:interrogativo formas)
        prons (:pronombre formas)
        res-num (analizar-numero lexema formas reglas)]
    (if (:error res-num)
      {:ok? false :error (:error res-num)}
      (let [candidatos
            (cond-> []
              (= lexema part-preg)
              (conj {:categoria :particula-pregunta})

              (contains? interrogs lexema)
              (conj {:categoria :interrogativo :rol-int (get interrogs lexema)})

              (contains? prons lexema)
              (conj {:categoria :pronombre})

              (some? res-num)
              (conj res-num)

              true
              (into (keep identity [(analizar-sustantivo lexema formas reglas)
                                    (analizar-verbo lexema formas reglas)])))]
        (if (seq candidatos)
          {:ok? true
           :token (assoc tok
                         :analisis candidatos
                         :categorias (set (map :categoria candidatos)))}
          {:ok? false
           :error (or (diagnosticar-morfologia-invalida lexema formas reglas)
                      (err :palabra-desconocida
                           (str "Palabra no reconocida en Kethari: '" lexema "'.")))})))))

(defn- analisis-de [tok cat]
  (first (filter #(= (:categoria %) cat) (:analisis tok))))

(defn- es-cat? [tok cat]
  (contains? (:categorias tok) cat))

;; --- Parser descendente recursivo de Kethari (SOV, D-004, D-013, D-019) ---

(defn- validar-concordancia-num-sust
  [tok-num tok-sust reglas]
  (let [valor (:valor (analisis-de tok-num :numero))
        plural? (:plural? (analisis-de tok-sust :sustantivo))
        suf-pl (lexer/obtener-regla reglas :sufijo.plural)]
    (cond
      (and (= valor 1) plural?)
      (err :concordancia-plural
           (str "El número 1 ('" (:lexema tok-num) "') requiere sustantivo singular, no '" (:lexema tok-sust) "'."))

      (and (not= valor 1) (not plural?))
      (err :concordancia-plural
           (str "En Kethari el sustantivo '" (:lexema tok-sust) "' con '" (:lexema tok-num)
                "' (" valor ") debe llevar el sufijo plural '-" suf-pl "'."))

      :else nil)))

(defn- parsear-sn
  "SN Kethari (D-013: poseedor, luego número, luego sustantivo):
    SN ::= Pronombre | [Poseedor(Pronombre)] [Numero] Sustantivo"
  [tokens idx limite reglas]
  (let [ok (atom [])
        errores (atom [])]
    (when (< idx limite)
      (let [t0 (nth tokens idx)]
        (when (es-cat? t0 :pronombre)
          (swap! ok conj {:nodo {:tipo :pronombre :lexema (:lexema t0)} :sig (inc idx)}))

        (when (es-cat? t0 :sustantivo)
          (swap! ok conj {:nodo (assoc (analisis-de t0 :sustantivo) :tipo :sustantivo :lexema (:lexema t0))
                          :sig (inc idx)}))

        (when (and (es-cat? t0 :numero)
                   (< (inc idx) limite)
                   (es-cat? (nth tokens (inc idx)) :sustantivo))
          (let [t1 (nth tokens (inc idx))]
            (if-let [e (validar-concordancia-num-sust t0 t1 reglas)]
              (swap! errores conj e)
              (swap! ok conj {:nodo {:tipo :cuantificado
                                     :numero (:valor (analisis-de t0 :numero))
                                     :sustantivo (:lexema t1)}
                              :sig (+ idx 2)}))))

        (when (and (es-cat? t0 :pronombre)
                   (< (inc idx) limite)
                   (es-cat? (nth tokens (inc idx)) :sustantivo))
          (let [t1 (nth tokens (inc idx))]
            (swap! ok conj {:nodo {:tipo :posesivo
                                   :poseedor (:lexema t0)
                                   :sustantivo (:lexema t1)}
                            :sig (+ idx 2)})))

        (when (and (es-cat? t0 :pronombre)
                   (< (+ idx 2) limite)
                   (es-cat? (nth tokens (inc idx)) :numero)
                   (es-cat? (nth tokens (+ idx 2)) :sustantivo))
          (let [t1 (nth tokens (inc idx))
                t2 (nth tokens (+ idx 2))]
            (if-let [e (validar-concordancia-num-sust t1 t2 reglas)]
              (swap! errores conj e)
              (swap! ok conj {:nodo {:tipo :posesivo-cuantificado
                                     :poseedor (:lexema t0)
                                     :numero (:valor (analisis-de t1 :numero))
                                     :sustantivo (:lexema t2)}
                              :sig (+ idx 3)}))))))
    {:ok @ok :errores @errores}))

(defn- parsear-un-sn-exacto
  [tokens inicio fin reglas]
  (let [{:keys [ok errores]} (parsear-sn tokens inicio fin reglas)]
    {:ok (vec (filter #(= (:sig %) fin) ok)) :errores errores}))

(defn- parsear-uno-o-dos-sns
  [tokens inicio fin reglas]
  (let [{ok1 :ok err1 :errores} (parsear-sn tokens inicio fin reglas)
        errores (atom (vec err1))
        sols (atom [])]
    (doseq [{n1 :nodo sig1 :sig} ok1]
      (if (= sig1 fin)
        (swap! sols conj {:sujeto n1 :objeto nil})
        (let [{ok2 :ok err2 :errores} (parsear-sn tokens sig1 fin reglas)]
          (swap! errores into err2)
          (doseq [{n2 :nodo sig2 :sig} ok2]
            (when (= sig2 fin)
              (swap! sols conj {:sujeto n1 :objeto n2}))))))
    {:ok @sols :errores @errores}))

(defn- verificar-regla-nosotros-d004
  "Verifica concordancia de `anir` (pasado) vs `anul` (presente/futuro) según D-004,
  usando únicamente las reglas `:nosotros.*` cargadas de la BD."
  [tokens tiempo-verbo reglas]
  (let [n-pas (lexer/obtener-regla reglas :nosotros.pasado)
        n-pre (lexer/obtener-regla reglas :nosotros.presente)
        n-fut (lexer/obtener-regla reglas :nosotros.futuro)
        formas-nosotros (set (remove nil? [n-pas n-pre n-fut]))
        esperada (case tiempo-verbo
                   :pasado n-pas
                   :presente n-pre
                   :futuro n-fut
                   nil)]
    (when (and esperada (> (count formas-nosotros) 1))
      (vec
        (keep (fn [tok]
                (let [lex (:lexema tok)]
                  (when (and (contains? formas-nosotros lex)
                             (not= lex esperada))
                    (err :concordancia-nosotros-tiempo
                         (str "En Kethari (bitácora D-004), con verbo en " (name tiempo-verbo)
                              " corresponde '" esperada "' y no '" lex "'.")))))
              tokens)))))

(defn- diagnosticar-sintaxis
  [tokens errores-internos]
  (let [n (count tokens)
        pos-ka (keep-indexed (fn [i t] (when (es-cat? t :particula-pregunta) i)) tokens)
        pos-int (keep-indexed (fn [i t] (when (es-cat? t :interrogativo) i)) tokens)
        pos-verb (keep-indexed (fn [i t] (when (es-cat? t :verbo) i)) tokens)
        sust-luego-num? (some (fn [[a b]]
                                (and (es-cat? a :sustantivo) (es-cat? b :numero)))
                              (partition 2 1 tokens))
        num-luego-pron-sust? (some (fn [[a b c]]
                                     (and (es-cat? a :numero)
                                          (es-cat? b :pronombre)
                                          (es-cat? c :sustantivo)))
                                   (partition 3 1 tokens))]
    (cond
      (seq errores-internos)
      (vec (distinct errores-internos))

      (or (> (count pos-ka) 1)
          (and (seq pos-ka) (not= (first pos-ka) (dec n))))
      [(err :posicion-invalida-ka
            "La partícula interrogativa 'ka' debe aparecer una sola vez al final de la oración Kethari.")]

      (> (count pos-int) 1)
      [(err :interrogativo-duplicado
            "No se permite más de una palabra interrogativa en Kethari.")]

      (and (seq pos-int) (not= (first pos-int) 0))
      [(err :interrogativo-mal-ubicado
            "En Kethari la palabra interrogativa debe ir al inicio de la oración.")]

      (and (seq pos-int) (empty? pos-ka))
      [(err :sin-particula-pregunta
            "Toda pregunta en Kethari debe terminar con la partícula 'ka'.")]

      (empty? pos-verb)
      [(err :sin-verbo "La oración Kethari no contiene ningún verbo válido.")]

      num-luego-pron-sust?
      [(err :orden-poseedor-numero
            "En Kethari (D-013) el poseedor va antes del número y del sustantivo (ej. 'ulesh ra-sel kumesh').")]

      sust-luego-num?
      [(err :orden-numero-sustantivo
            "En Kethari el número debe ir delante del sustantivo.")]

      (let [idx-v (if (seq pos-ka) (- n 2) (dec n))]
        (not (and (>= idx-v 0) (es-cat? (nth tokens idx-v) :verbo))))
      [(err :orden-invalido
            "En Kethari el orden es Sujeto-Objeto-Verbo (SOV): el verbo va al final (antes de 'ka' en preguntas).")]

      (or (and (= n 1) (es-cat? (first tokens) :verbo))
          (and (= n 2) (seq pos-ka) (es-cat? (first tokens) :verbo)))
      [(err :sin-sujeto "En Kethari el sujeto es obligatorio y no puede omitirse.")]

      :else
      [(err :orden-invalido "La oración no cumple la gramática SOV de Kethari.")])))

(defn- parsear-oracion
  [tokens reglas]
  (let [n (count tokens)
        tiene-ka? (and (pos? n) (es-cat? (peek tokens) :particula-pregunta))
        fin-sin-ka (if tiene-ka? (dec n) n)
        errores-acum (atom [])]
    (if (< fin-sin-ka 2)
      {:ok? false :errores (diagnosticar-sintaxis tokens [])}
      (let [idx-verbo (dec fin-sin-ka)
            tok-verbo (nth tokens idx-verbo)
            verbo-info (analisis-de tok-verbo :verbo)
            tiene-int? (es-cat? (first tokens) :interrogativo)]
        (cond
          (not verbo-info)
          {:ok? false :errores (diagnosticar-sintaxis tokens [])}

          (and tiene-int? (not tiene-ka?))
          {:ok? false :errores (diagnosticar-sintaxis tokens [])}

          :else
          (let [res-sint
                (if tiene-int?
                  (let [tok-int (first tokens)
                        rol-int (:rol-int (analisis-de tok-int :interrogativo))]
                    (case rol-int
                      ;; sem [SN] V ka (cubre "¿Quién envió el mensaje?" y "¿A quién escuchan ustedes?")
                      :sujeto
                      (if (= 1 idx-verbo)
                        {:ok? true}
                        (let [{:keys [ok errores]} (parsear-un-sn-exacto tokens 1 idx-verbo reglas)]
                          (swap! errores-acum into errores)
                          (if (seq ok) {:ok? true} {:ok? false})))

                      ;; kel SUJ V ka
                      :objeto
                      (let [{:keys [ok errores]} (parsear-un-sn-exacto tokens 1 idx-verbo reglas)]
                        (swap! errores-acum into errores)
                        (if (seq ok) {:ok? true} {:ok? false}))

                      ;; kom / dor SUJ [OBJ] V ka
                      (:tiempo :lugar :adverbial)
                      (let [{:keys [ok errores]} (parsear-uno-o-dos-sns tokens 1 idx-verbo reglas)]
                        (swap! errores-acum into errores)
                        (if (seq ok) {:ok? true} {:ok? false}))

                      ;; kai SustantivoPlural [SN] V ka
                      :cantidad
                      (if (and (< 1 idx-verbo) (es-cat? (nth tokens 1) :sustantivo))
                        (let [sust-info (analisis-de (nth tokens 1) :sustantivo)]
                          (if-not (:plural? sust-info)
                            (do
                              (swap! errores-acum conj
                                     (err :concordancia-plural
                                          (str "Después de '" (:lexema tok-int) "' el sustantivo debe llevar '-"
                                               (lexer/obtener-regla reglas :sufijo.plural) "'.")))
                              {:ok? false})
                            (if (= 2 idx-verbo)
                              {:ok? true}
                              (let [{:keys [ok errores]} (parsear-un-sn-exacto tokens 2 idx-verbo reglas)]
                                (swap! errores-acum into errores)
                                (if (seq ok) {:ok? true} {:ok? false})))))
                        (do
                          (swap! errores-acum conj
                                 (err :orden-invalido
                                      (str "En Kethari '" (:lexema tok-int) "' debe ir seguido de un sustantivo plural.")))
                          {:ok? false}))))

                  ;; Declarativa o pregunta sí/no: SUJ [OBJ] V [ka]
                  (let [{:keys [ok errores]} (parsear-uno-o-dos-sns tokens 0 idx-verbo reglas)]
                    (swap! errores-acum into errores)
                    (if (seq ok) {:ok? true} {:ok? false})))]
            (if-not (:ok? res-sint)
              {:ok? false :errores (diagnosticar-sintaxis tokens @errores-acum)}
              (let [errs-d004 (verificar-regla-nosotros-d004 tokens (:tiempo verbo-info) reglas)]
                (if (seq errs-d004)
                  {:ok? false :errores errs-d004}
                  {:ok? true :errores []})))))))))

;; --- API pública oficial ---

(defn validar
  "Devuelve `{:valida? true|false :errores [{:fase :tipo :mensaje}]}`.
  - Aridad 1 (contrato oficial): consulta el léxico y las reglas vigentes en `axios.bd`.
  - Aridad 2 (pruebas aisladas): recibe `{:formas ... :reglas ...}`."
  ([texto]
   (let [reglas (bd/reglas :kethari)
         formas (lexer/cargar-formas-categorizadas :kethari reglas)]
     (validar texto {:formas formas :reglas reglas})))
  ([texto {:keys [formas reglas]}]
   (if (or (nil? texto) (and (string? texto) (empty? (.trim ^String texto))))
     {:valida? false
      :errores [(err :entrada-vacia "La traducción Kethari está vacía.")]}
     (let [alfabeto (lexer/derivar-alfabeto formas reglas)
           res-lex (lexer/tokenizar fase texto alfabeto (lexer/obtener-regla reglas :numeros.separador))]
       (if-not (:ok? res-lex)
         {:valida? false :errores (:errores res-lex)}
         (let [clasificados (mapv #(clasificar-token % formas reglas) (:tokens res-lex))
               errs-morf (vec (keep :error clasificados))]
           (if (seq errs-morf)
             {:valida? false :errores errs-morf}
             (let [tokens-ok (mapv :token clasificados)
                   res-sint (parsear-oracion tokens-ok reglas)]
               {:valida? (boolean (:ok? res-sint))
                :errores (vec (:errores res-sint))}))))))))
