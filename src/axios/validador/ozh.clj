(ns axios.validador.ozh
  "Validador de Ozh: analizador léxico y sintáctico propio, independiente
  del generador (contratos.md, sección 7). Condición 2 de Axios.
  Responsable: Jhojan (PP-26).

  Recibe SOLO el texto y lo vuelve a leer. Maneja los símbolos ʔ y ü.
  Rechaza oraciones sin partícula. Acepta el pasado negado sin ʔoka (D-012)
  y el prefijo ʔa- delante de un verbo que ya empieza por ʔ (ʔaʔim, D-008).
  Todo el léxico y las reglas se leen de la BD."
  (:require [axios.bd :as bd]
            [axios.validador.lexer :as lexer]))

(def ^:private fase :validador-ozh)

(defn- err [tipo mensaje]
  {:fase fase :tipo tipo :mensaje mensaje})

;; --- Análisis morfológico dirigido por reglas de BD ---

(defn- analizar-numero
  [lexema formas reglas]
  (let [digitos (:digito formas)
        sep-str (lexer/obtener-regla reglas :numeros.separador)
        sep (when (seq sep-str) (first sep-str))
        base (lexer/parse-entero (lexer/obtener-regla reglas :numeros.base) 10)
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
                       (str "Número Ozh inválido '" lexema "': no se permiten ceros a la izquierda."))}

          (> valor maximo)
          {:error (err :numero-fuera-de-rango
                       (str "Número Ozh '" lexema "' (" valor ") supera el máximo permitido (" maximo ")."))}

          :else
          {:categoria :numero :valor valor :base base}))

      tiene-sep?
      {:error (err :numero-invalido
                   (str "Número Ozh inválido '" lexema "': contiene cifras no válidas en base " base "."))}

      :else nil)))

(defn- analizar-verbo
  [lexema formas reglas]
  (let [verbos (:verbo formas)
        pref-preg (lexer/obtener-regla reglas :prefijo.pregunta)]
    (cond
      (contains? verbos lexema)
      {:categoria :verbo :raiz lexema :pregunta? false}

      (and (lexer/empieza-con? lexema pref-preg)
           (contains? verbos (lexer/quitar-prefijo lexema pref-preg)))
      {:categoria :verbo :raiz (lexer/quitar-prefijo lexema pref-preg) :pregunta? true}

      :else nil)))

(defn- diagnosticar-morfologia-invalida
  [lexema formas reglas]
  (let [verbos (:verbo formas)
        pref-preg (lexer/obtener-regla reglas :prefijo.pregunta)
        neg (lexer/obtener-regla reglas :negacion)]
    (cond
      ;; Doble prefijo interrogativo (ej. ʔaʔasur)
      (and (lexer/empieza-con? lexema (str pref-preg pref-preg))
           (contains? verbos (lexer/quitar-prefijo lexema (str pref-preg pref-preg))))
      (err :morfologia-invalida
           (str "Verbo Ozh inválido '" lexema "': prefijo interrogativo '" pref-preg "' duplicado."))

      ;; Negación pegada al verbo (en Ozh la negación es palabra separada)
      (and (seq neg)
           (lexer/empieza-con? lexema neg)
           (contains? verbos (lexer/quitar-prefijo lexema neg)))
      (err :morfologia-invalida
           (str "Verbo Ozh inválido '" lexema "': la negación '" neg "' debe ir separada delante del verbo."))

      :else nil)))

(defn- clasificar-token
  [tok formas reglas]
  (let [{:keys [lexema]} tok
        m-pas (lexer/obtener-regla reglas :marcador.pasado)
        m-fut (lexer/obtener-regla reglas :marcador.futuro)
        neg (lexer/obtener-regla reglas :negacion)
        particulas (or (not-empty (:particula formas))
                       (set (remove nil? [(lexer/obtener-regla reglas :particula.pregunta.defecto)
                                          (lexer/obtener-regla reglas :particula.afirmacion.pasado)
                                          (lexer/obtener-regla reglas :particula.afirmacion.terceros)
                                          (lexer/obtener-regla reglas :particula.afirmacion.participantes)])))
        res-num (analizar-numero lexema formas reglas)]
    (if (:error res-num)
      {:ok? false :error (:error res-num)}
      (let [candidatos
            (cond-> []
              (= lexema m-pas)
              (conj {:categoria :marcador-tiempo :tiempo :pasado})

              (= lexema m-fut)
              (conj {:categoria :marcador-tiempo :tiempo :futuro})

              (= lexema neg)
              (conj {:categoria :negacion})

              (contains? particulas lexema)
              (conj {:categoria :particula})

              (contains? (:interrogativo formas) lexema)
              (conj {:categoria :interrogativo :rol-int (get (:interrogativo formas) lexema)})

              (contains? (:pronombre formas) lexema)
              (conj {:categoria :pronombre})

              (some? res-num)
              (conj res-num)

              (contains? (:sustantivo formas) lexema)
              (conj {:categoria :sustantivo :raiz lexema})

              true
              (into (keep identity [(analizar-verbo lexema formas reglas)])))]
        (if (seq candidatos)
          {:ok? true
           :token (assoc tok
                         :analisis candidatos
                         :categorias (set (map :categoria candidatos)))}
          {:ok? false
           :error (or (diagnosticar-morfologia-invalida lexema formas reglas)
                      (err :palabra-desconocida
                           (str "Palabra no reconocida en Ozh: '" lexema "'.")))})))))

(defn- analisis-de [tok cat]
  (first (filter #(= (:categoria %) cat) (:analisis tok))))

(defn- es-cat? [tok cat]
  (contains? (:categorias tok) cat))

;; --- Parser descendente recursivo de Ozh (VSO + partícula obligatoria) ---

(defn- parsear-sn
  "SN Ozh (poseedor detrás de lo poseído, número delante del sustantivo):
    SN ::= Pronombre | [Numero] Sustantivo [Poseedor(Pronombre)]"
  [tokens idx limite]
  (let [ok (atom [])]
    (when (< idx limite)
      (let [t0 (nth tokens idx)]
        (when (es-cat? t0 :pronombre)
          (swap! ok conj {:nodo {:tipo :pronombre :lexema (:lexema t0)} :sig (inc idx)}))

        (when (es-cat? t0 :sustantivo)
          (swap! ok conj {:nodo {:tipo :sustantivo :lexema (:lexema t0)} :sig (inc idx)}))

        (when (and (es-cat? t0 :sustantivo)
                   (< (inc idx) limite)
                   (es-cat? (nth tokens (inc idx)) :pronombre))
          (swap! ok conj {:nodo {:tipo :posesivo
                                 :sustantivo (:lexema t0)
                                 :poseedor (:lexema (nth tokens (inc idx)))}
                          :sig (+ idx 2)}))

        (when (and (es-cat? t0 :numero)
                   (< (inc idx) limite)
                   (es-cat? (nth tokens (inc idx)) :sustantivo))
          (swap! ok conj {:nodo {:tipo :cuantificado
                                 :numero (:valor (analisis-de t0 :numero))
                                 :sustantivo (:lexema (nth tokens (inc idx)))}
                          :sig (+ idx 2)}))

        (when (and (es-cat? t0 :numero)
                   (< (+ idx 2) limite)
                   (es-cat? (nth tokens (inc idx)) :sustantivo)
                   (es-cat? (nth tokens (+ idx 2)) :pronombre))
          (swap! ok conj {:nodo {:tipo :cuantificado-posesivo
                                 :numero (:valor (analisis-de t0 :numero))
                                 :sustantivo (:lexema (nth tokens (inc idx)))
                                 :poseedor (:lexema (nth tokens (+ idx 2)))}
                          :sig (+ idx 3)}))))
    @ok))

(defn- parsear-un-sn-exacto [tokens inicio fin]
  (vec (filter #(= (:sig %) fin) (parsear-sn tokens inicio fin))))

(defn- parsear-uno-o-dos-sns [tokens inicio fin]
  (let [sols (atom [])]
    (doseq [{n1 :nodo sig1 :sig} (parsear-sn tokens inicio fin)]
      (if (= sig1 fin)
        (swap! sols conj {:sujeto n1 :objeto nil})
        (doseq [{n2 :nodo sig2 :sig} (parsear-sn tokens sig1 fin)]
          (when (= sig2 fin)
            (swap! sols conj {:sujeto n1 :objeto n2})))))
    @sols))

(defn- validar-marcadores-globales
  [tokens]
  (let [n (count tokens)
        pos-tiempo (vec (keep-indexed (fn [i t] (when (es-cat? t :marcador-tiempo) i)) tokens))
        pos-neg (vec (keep-indexed (fn [i t] (when (es-cat? t :negacion) i)) tokens))
        pos-int (vec (keep-indexed (fn [i t] (when (es-cat? t :interrogativo) i)) tokens))
        pos-part (vec (keep-indexed (fn [i t] (when (es-cat? t :particula) i)) tokens))
        pos-verb (vec (keep-indexed (fn [i t] (when (es-cat? t :verbo) i)) tokens))]
    (cond
      (> (count pos-tiempo) 1)
      [(err :marcador-tiempo-duplicado
            "No se permite más de un marcador de tiempo en Ozh.")]

      (and (= (count pos-tiempo) 1) (not= (first pos-tiempo) 0))
      [(err :marcador-tiempo-mal-ubicado
            "En Ozh el marcador de tiempo debe ir al inicio de la oración.")]

      (> (count pos-neg) 1)
      [(err :negacion-duplicada "No se permite duplicar la negación en Ozh.")]

      (> (count pos-int) 1)
      [(err :interrogativo-duplicado "No se permite más de una palabra interrogativa en Ozh.")]

      (and (= (count pos-int) 1) (not= (first pos-int) (dec n)))
      [(err :interrogativo-mal-ubicado
            "En Ozh la palabra interrogativa debe ir al final de la oración.")]

      (empty? pos-verb)
      [(err :sin-verbo "La oración Ozh no contiene ningún verbo válido.")]

      (empty? pos-part)
      [(err :sin-particula "Falta la partícula después del verbo")]

      (> (count pos-part) 1)
      [(err :particula-duplicada "La oración Ozh contiene más de una partícula evidencial.")]

      :else nil)))

(defn- parsear-oracion
  [tokens reglas]
  (if-let [errs-glob (validar-marcadores-globales tokens)]
    {:ok? false :errores errs-glob}
    (let [n (count tokens)
          idx1 (if (and (< 0 n) (es-cat? (nth tokens 0) :marcador-tiempo)) 1 0)
          idx2 (if (and (< idx1 n) (es-cat? (nth tokens idx1) :negacion)) (inc idx1) idx1)
          tok-verbo (when (< idx2 n) (nth tokens idx2))
          verbo-info (when tok-verbo (analisis-de tok-verbo :verbo))]
      (cond
        (not verbo-info)
        {:ok? false
         :errores [(err :orden-invalido
                        "En Ozh el orden es Verbo-Sujeto-Objeto (VSO): el verbo debe ir al inicio (tras marcador de tiempo y/o negación).")]}

        (not (and (< (inc idx2) n) (es-cat? (nth tokens (inc idx2)) :particula)))
        {:ok? false
         :errores [(err :sin-particula "Falta la partícula después del verbo")]}

        :else
        (let [idx-args (+ idx2 2)
              tiene-int? (and (< idx-args n) (es-cat? (peek tokens) :interrogativo))
              tok-int (when tiene-int? (peek tokens))
              fin-args (if tiene-int? (dec n) n)
              verbo-preg? (:pregunta? verbo-info)]
          (cond
            (and tiene-int? (not verbo-preg?))
            {:ok? false
             :errores [(err :sin-prefijo-pregunta
                            (str "Las preguntas en Ozh requieren el prefijo '"
                                 (lexer/obtener-regla reglas :prefijo.pregunta) "' en el verbo."))]}

            tiene-int?
            (let [rol (:rol-int (analisis-de tok-int :interrogativo))]
              (case rol
                (:tiempo :lugar :adverbial)
                (if (seq (parsear-uno-o-dos-sns tokens idx-args fin-args))
                  {:ok? true :errores []}
                  {:ok? false
                   :errores [(err :orden-invalido
                                  "Los argumentos después de la partícula no forman un Sujeto [Objeto] válido en Ozh.")]})

                :objeto
                (if (or (= idx-args fin-args)
                        (seq (parsear-un-sn-exacto tokens idx-args fin-args)))
                  {:ok? true :errores []}
                  {:ok? false
                   :errores [(err :orden-invalido
                                  "Estructura inválida en pregunta de objeto en Ozh.")]})

                ;; wis: cubre tanto "¿Quién envió el mensaje?" como señal día 9 "¿A quién escuchan ustedes?"
                :sujeto
                (if (or (= idx-args fin-args)
                        (seq (parsear-un-sn-exacto tokens idx-args fin-args)))
                  {:ok? true :errores []}
                  {:ok? false
                   :errores [(err :orden-invalido
                                  "Estructura inválida en pregunta con interrogativo de persona en Ozh.")]})

                :cantidad
                (if (seq (parsear-uno-o-dos-sns tokens idx-args fin-args))
                  {:ok? true :errores []}
                  {:ok? false
                   :errores [(err :orden-invalido
                                  "Estructura inválida en pregunta de cantidad en Ozh.")]})))

            (= idx-args fin-args)
            {:ok? false
             :errores [(err :sin-sujeto "En Ozh el sujeto es obligatorio después de la partícula.")]}

            :else
            (if (seq (parsear-uno-o-dos-sns tokens idx-args fin-args))
              {:ok? true :errores []}
              {:ok? false
               :errores [(err :orden-invalido
                              "Los argumentos en Ozh no cumplen VSO, número antes del sustantivo ni poseedor detrás de lo poseído.")]})))))))

;; --- API pública oficial ---

(defn validar
  "Devuelve `{:valida? true|false :errores [{:fase :tipo :mensaje}]}`.
  - Aridad 1 (contrato oficial): consulta el léxico y las reglas vigentes en `axios.bd`.
  - Aridad 2 (pruebas aisladas): recibe `{:formas ... :reglas ...}`."
  ([texto]
   (let [reglas (bd/reglas :ozh)
         formas (lexer/cargar-formas-categorizadas :ozh reglas)]
     (validar texto {:formas formas :reglas reglas})))
  ([texto {:keys [formas reglas]}]
   (if (or (nil? texto) (and (string? texto) (empty? (.trim ^String texto))))
     {:valida? false
      :errores [(err :entrada-vacia "La traducción Ozh está vacía.")]}
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
