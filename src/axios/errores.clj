(ns axios.errores
  "Formato único de errores entre fases, manejo de palabras desconocidas y
  explicaciones de punta a punta (contratos.md, sección 8; bitácora D-005, D-009, D-014).
  Responsable: Jhojan (PP-27, PP-28)."
  (:require [axios.bd :as bd]
            [clojure.string :as str]))

;; --- 1. Formato único de errores entre fases (Contratos, sección 8) ---

(defn error-de-fase
  "Lanza un error con el formato acordado por el equipo:
  `{:fase fase :tipo tipo :detalle detalle}`.
  Ej: `(error-de-fase :parser :estructura-invalida \"Falta el verbo\" {:pos 12})`"
  ([fase tipo mensaje]
   (error-de-fase fase tipo mensaje {}))
  ([fase tipo mensaje detalle]
   (throw (ex-info mensaje
                   {:fase fase
                    :tipo tipo
                    :detalle (or detalle {})}))))

(defn extraer-error
  "Normaliza cualquier excepción (`ExceptionInfo` o `Throwable` inesperado) a un
  mapa `{:fase :tipo :mensaje :detalle}` para que nunca se pierda una pregunta."
  [^Throwable ex]
  (let [datos (ex-data ex)]
    (if (and (map? datos) (:fase datos) (:tipo datos))
      {:fase (:fase datos)
       :tipo (:tipo datos)
       :mensaje (.getMessage ex)
       :detalle (or (:detalle datos) {})}
      {:fase :pipeline
       :tipo :error-interno
       :mensaje (or (.getMessage ex) (.getName (class ex)))
       :detalle {:clase (.getName (class ex))}})))

;; --- 2. Manejo de palabras desconocidas (PP-27, D-005, D-014) ---

(defn mensaje-tierra-sin-kethari
  "Explicación exacta acordada en la bitácora D-005 tras la corrección del manual 0.8."
  []
  "'tierra' no tiene equivalente en Kethari desde la corrección del manual 0.8; si se refiere al planeta, use 'planeta' o 'mundo'.")

(defn detectar-desconocidas
  "Recorre los tokens entregados por morfología o el lexer y retorna un vector con
  las palabras sin respaldo en el manual ni en las señales registradas (D-014).
  No inventa traducciones."
  ([tokens]
   (detectar-desconocidas tokens (bd/vocabulario-es)))
  ([tokens vocabulario-es]
   (vec
     (keep (fn [tok]
             (cond
               (= (:categoria tok) :desconocida)
               {:lexema (:lexema tok)
                :pos (:pos tok)
                :motivo (str "'" (:lexema tok) "' no tiene equivalente en Kethari ni en Ozh.")}

               (and (= (:tipo tok) :palabra)
                    (nil? (:categoria tok))
                    (not (contains? vocabulario-es (:lexema tok))))
               {:lexema (:lexema tok)
                :pos (:pos tok)
                :motivo (str "'" (:lexema tok) "' no está respaldada por el manual ni por señales registradas.")}

               :else nil))
           tokens))))

(defn verificar-desconocidas!
  "Retiene la pregunta lanzando `error-de-fase` con `:tipo :palabra-desconocida` si
  encuentra palabras sin respaldo léxico (condición 3: no se inventa nada)."
  ([tokens]
   (verificar-desconocidas! tokens (bd/vocabulario-es)))
  ([tokens vocabulario-es]
   (let [halladas (detectar-desconocidas tokens vocabulario-es)]
     (when (seq halladas)
       (let [lista (str/join ", " (map #(str "'" (:lexema %) "'") halladas))]
         (error-de-fase :morfologia
                        :palabra-desconocida
                        (str "No se puede traducir sin inventar vocabulario. Palabra(s) desconocida(s): "
                             lista ".")
                        {:palabras halladas}))))))

(defn validar-concepto-en-lengua!
  "Auxiliar para los generadores (PP-22, PP-23): si `(bd/palabra-en concepto lengua)`
  es `nil`, lanza `error-de-fase` de `:generador` con `:tipo :palabra-desconocida`
  (incluye el mensaje específico de D-005 para `:tierra` en Kethari)."
  [concepto lengua forma-bd]
  (when (nil? forma-bd)
    (let [msg (if (and (= concepto :tierra) (= lengua :kethari))
                (mensaje-tierra-sin-kethari)
                (str "'" (name concepto) "' no tiene equivalente registrado en "
                     (case lengua :kethari "Kethari" :ozh "Ozh" (name lengua)) "."))]
      (error-de-fase :generador
                     :palabra-desconocida
                     msg
                     {:concepto concepto :lengua lengua})))
  forma-bd)

;; --- 3. Explicaciones legibles por fase y por validación (PP-28, D-009) ---

(def ^:private nombres-fase
  {:normalizador      "normalizador"
   :lexer             "analizador léxico"
   :corrector         "corrector ortográfico"
   :morfologia        "análisis morfológico"
   :parser            "analizador sintáctico"
   :generador         "generador"
   :validador-kethari "validador Kethari"
   :validador-ozh     "validador Ozh"
   :pipeline          "pipeline"})

(defn explicar
  "Convierte los datos de un error (`{:fase :tipo :detalle}`) en una explicación
  clara y legible en español para el usuario y el historial (usada por `axios.pipeline`)."
  [{:keys [fase tipo detalle]} mensaje]
  (let [nombre-fase (get nombres-fase fase (if fase (name fase) "sistema"))
        detalle-pos (when-let [p (:pos detalle)]
                      (if (map? p)
                        (str " (posición " (:inicio p) "–" (:fin p) ")")
                        (str " (posición " p ")")))]
    (case tipo
      :entrada-vacia
      "La pregunta está vacía. Ingrese una pregunta en español."

      :palabra-desconocida
      (str "No se transmitió porque Axios prohíbe inventar vocabulario (" nombre-fase "): "
           mensaje)

      :caracter-invalido
      (str "Carácter no reconocido en " nombre-fase detalle-pos ": " mensaje)

      :numero-fuera-de-rango
      (str "Cantidad fuera del rango permitido (0 a 9.999) en " nombre-fase ": " mensaje)

      :estructura-invalida
      (str "La pregunta no cumple la gramática soportada (" nombre-fase "): " mensaje)

      :verbo-no-reconocido
      (str "No se reconoció la forma verbal en " nombre-fase ": " mensaje)

      :sin-particula
      (str "Traducción rechazada por el " nombre-fase ": " mensaje)

      (str "No se pudo traducir (" nombre-fase
           (when tipo (str ", " (name tipo)))
           ")" detalle-pos ": " mensaje))))

(defn explicar-rechazo-validadores
  "Construye la explicación detallada cuando una o ambas traducciones no pasan los
  validadores independientes (D-009: si falla una lengua, no se envía ninguna)."
  [res-kethari res-ozh]
  (let [resumen-lengua (fn [etiqueta res]
                         (when-not (:valida? res)
                           (let [msgs (map :mensaje (:errores res))]
                             (str etiqueta ": "
                                  (if (seq msgs)
                                    (str/join "; " msgs)
                                    "traducción inválida")))))
        partes (remove nil? [(resumen-lengua "Kethari" res-kethari)
                             (resumen-lengua "Ozh" res-ozh)])]
    (str "No se transmitió ninguna traducción porque la validación falló (D-009). "
         (str/join " | " partes))))
