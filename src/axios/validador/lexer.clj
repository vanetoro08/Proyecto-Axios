(ns axios.validador.lexer
  "Autómata Finito Determinista (AFD) manual, carácter por carácter, compartido por
  los validadores independientes de Kethari (PP-25) y Ozh (PP-26).

  Cumple las restricciones del enunciado (secciones 5.1 y 5.2):
  - Sin expresiones regulares ni división basada en patrones.
  - Sin generadores de analizadores.
  - Nada del léxico ni de las reglas escrito en el código: todo se consulta desde `axios.bd`."
  (:require [axios.bd :as bd]))

;; --- Carga dinámica de formas categorizadas desde `axios.bd` ---

(def ^:private digitos-concepto
  [[:cero 0] [:uno 1] [:dos 2] [:tres 3] [:cuatro 4]
   [:cinco 5] [:seis 6] [:siete 7] [:ocho 8] [:nueve 9]])

(def ^:private roles-interrogativo
  {:quien   :sujeto
   :que     :objeto
   :cuando  :tiempo
   :donde   :lugar
   :cuantos :cantidad})

(defn obtener-regla
  "Lee una clave de regla desde el mapa de `(bd/reglas lengua)` soportando tanto
  claves keyword (`:sufijo.pasado`, devuelto por `axios.bd`) como string."
  [reglas clave-kw]
  (or (get reglas clave-kw)
      (get reglas (name clave-kw))))

(defn cargar-formas-categorizadas
  "Construye en tiempo de ejecución el índice categorizado de formas vigentes para
  `lengua` (`:kethari` u `:ozh`) consultando únicamente la API pública de `axios.bd`.

  - Recorre `(bd/vocabulario-es)` + `(bd/buscar-es ...)` + `(bd/palabra-en ...)`
    para clasificar pronombres, sustantivos, verbos e interrogativos.
  - Incorpora variantes de pronombres/partículas declaradas en `(bd/reglas lengua)`
    (ej. `anir`/`anul` por D-004, `zi`/`mar`/`tho` por D-008/D-016).
  - Lee los dígitos vigentes mediante `(bd/palabra-en <concepto-digito> lengua)`.
  - Incorpora cualquier forma nueva que exista en `(bd/formas lengua)` tras un
    `cargar <archivo>` sin requerir recompilación."
  [lengua reglas]
  (let [formas-vigentes (set (bd/formas lengua))
        base {:pronombre #{}
              :sustantivo #{}
              :verbo #{}
              :particula #{}
              :interrogativo {}
              :digito {}}
        ;; 1. Clasificar a partir de conceptos del vocabulario español vigente
        con-vocab
        (reduce (fn [acc palabra-es]
                  (if-let [{:keys [concepto categoria]} (bd/buscar-es palabra-es)]
                    (if-let [{:keys [forma]} (bd/palabra-en concepto lengua)]
                      (if (contains? formas-vigentes forma)
                        (case categoria
                          :pronombre (update acc :pronombre conj forma)
                          :sustantivo (update acc :sustantivo conj forma)
                          :verbo (update acc :verbo conj forma)
                          :interrogativo (assoc-in acc [:interrogativo forma]
                                                   (get roles-interrogativo concepto :adverbial))
                          acc)
                        acc)
                      acc)
                    acc))
                base
                (bd/vocabulario-es))
        ;; 2. Agregar dígitos vigentes desde BD (manual 3.2)
        con-digitos
        (reduce (fn [acc [concepto valor]]
                  (if-let [{:keys [forma]} (bd/palabra-en concepto lengua)]
                    (if (contains? formas-vigentes forma)
                      (assoc-in acc [:digito forma] valor)
                      acc)
                    acc))
                con-vocab
                digitos-concepto)
        ;; 3. Variantes de pronombres (ej. anir/anul en D-004) y partículas evidenciales (D-008/D-016)
        prons-regla (remove nil? [(obtener-regla reglas :nosotros.pasado)
                                  (obtener-regla reglas :nosotros.presente)
                                  (obtener-regla reglas :nosotros.futuro)])
        parts-regla (remove nil? [(obtener-regla reglas :particula.pregunta.defecto)
                                  (obtener-regla reglas :particula.afirmacion.pasado)
                                  (obtener-regla reglas :particula.afirmacion.terceros)
                                  (obtener-regla reglas :particula.afirmacion.participantes)])
        con-reglas (-> con-digitos
                       (update :pronombre into (filter formas-vigentes prons-regla))
                       (update :particula into parts-regla))
        ;; 4. Cualquier forma vigente nueva (cargada por señal sin fila en español aún)
        ya-clasificadas (into #{}
                              (concat (:pronombre con-reglas)
                                      (:sustantivo con-reglas)
                                      (:verbo con-reglas)
                                      (:particula con-reglas)
                                      (keys (:interrogativo con-reglas))
                                      (keys (:digito con-reglas))))
        nuevas (remove ya-clasificadas formas-vigentes)]
    (update con-reglas :sustantivo into nuevas)))

;; --- Utilidades puras de cadena (sin expresiones regulares) ---

(defn dividir-por-caracter
  "Divide `s` por el carácter `sep` recorriendo índice por índice."
  [^String s ^Character sep]
  (let [n (.length s)]
    (loop [i 0 inicio 0 partes []]
      (if (= i n)
        (conj partes (.substring s inicio n))
        (if (= (.charAt s i) sep)
          (recur (inc i) (inc i) (conj partes (.substring s inicio i)))
          (recur (inc i) inicio partes))))))

(defn empieza-con?
  [^String s ^String pref]
  (and (seq pref) (.startsWith s pref) (> (.length s) (.length pref))))

(defn termina-con?
  [^String s ^String suf]
  (and (seq suf) (.endsWith s suf) (> (.length s) (.length suf))))

(defn quitar-prefijo [^String s ^String pref] (.substring s (.length pref)))

(defn quitar-sufijo [^String s ^String suf] (.substring s 0 (- (.length s) (.length suf))))

(defn parse-entero
  [v defecto]
  (cond
    (integer? v) v
    (string? v) (try (Long/parseLong v) (catch Exception _ defecto))
    :else defecto))

;; --- AFD manual carácter por carácter ---

(def ^:private letras-latinas-minusculas
  (set (map char (range (int \a) (inc (int \z))))))

(defn- espacio?
  [ch]
  (or (= ch \space) (= ch \tab) (= ch \newline) (= ch \return)))

(defn derivar-alfabeto
  "Deriva el conjunto de caracteres permitidos de una lengua a partir de las letras
  latinas minúsculas más cualquier carácter especial (como ʔ o ü en Ozh) presente en
  las formas y valores de reglas vigentes en la BD."
  [formas reglas]
  (let [cadenas (concat
                  (mapcat (fn [[_ v]]
                            (cond
                              (set? v) v
                              (map? v) (keys v)
                              (string? v) [v]
                              :else nil))
                          formas)
                  (filter string? (vals reglas)))]
    (reduce (fn [acc ^String s]
              (if (or (empty? s)
                      (.contains s ",")
                      (.contains s ".")
                      (Character/isDigit (.charAt s 0)))
                acc
                (reduce (fn [a ch]
                          (if (Character/isUpperCase ^Character ch) a (conj a ch)))
                        acc
                        s)))
            letras-latinas-minusculas
            cadenas)))

(defn- cerrar-token
  [fase buf inicio fin separador]
  (let [lexema (str buf)]
    (if (and separador (= (last lexema) separador))
      {:ok? false
       :error {:fase fase
               :tipo :guion-mal-ubicado
               :mensaje (str "El token '" lexema "' no puede terminar con '" separador "'.")}}
      {:ok? true
       :token {:lexema lexema
               :pos {:inicio inicio :fin (inc fin)}}})))

(defn tokenizar
  "Ejecuta un AFD manual sobre `texto` con tres estados:
  - :inicio     -> omite espacios en blanco
  - :en-palabra -> acumula caracteres válidos del alfabeto de la lengua
  - :en-guion   -> tras un separador numérico '-', exige un carácter no-separador

  Retorna {:ok? boolean :tokens [{:lexema \"...\" :pos {:inicio i :fin j}}] :errores [...]}."
  [fase texto alfabeto separador-str]
  (let [separador (when (seq separador-str) (first separador-str))
        alfabeto-con-sep (if separador (conj alfabeto separador) alfabeto)]
    (if-not (string? texto)
      {:ok? false
       :tokens []
       :errores [{:fase fase
                  :tipo :entrada-no-textual
                  :mensaje "La traducción a validar debe ser una cadena de texto."}]}
      (let [n (.length ^String texto)]
        (loop [i 0
               estado :inicio
               inicio-tok 0
               buf (StringBuilder.)
               tokens []
               errores []]
          (if (= i n)
            (if (= estado :inicio)
              {:ok? (empty? errores) :tokens tokens :errores errores}
              (let [res (cerrar-token fase buf inicio-tok (dec i) separador)]
                (if (:ok? res)
                  {:ok? (empty? errores)
                   :tokens (conj tokens (:token res))
                   :errores errores}
                  {:ok? false
                   :tokens tokens
                   :errores (conj errores (:error res))})))
            (let [ch (.charAt ^String texto i)]
              (cond
                (espacio? ch)
                (if (= estado :inicio)
                  (recur (inc i) :inicio 0 (StringBuilder.) tokens errores)
                  (let [res (cerrar-token fase buf inicio-tok (dec i) separador)]
                    (if (:ok? res)
                      (recur (inc i) :inicio 0 (StringBuilder.) (conj tokens (:token res)) errores)
                      (recur (inc i) :inicio 0 (StringBuilder.) tokens (conj errores (:error res))))))

                (not (contains? alfabeto-con-sep ch))
                (let [err {:fase fase
                           :tipo :caracter-invalido
                           :mensaje (str "Carácter no permitido '" ch "' en la posición " i ".")}]
                  (recur (inc i) :inicio 0 (StringBuilder.) tokens (conj errores err)))

                (and separador (= ch separador))
                (case estado
                  :inicio
                  (recur (inc i)
                         :inicio
                         0
                         (StringBuilder.)
                         tokens
                         (conj errores {:fase fase
                                        :tipo :guion-mal-ubicado
                                        :mensaje (str "Un token no puede comenzar con '" separador "' en la posición " i ".")}))

                  :en-guion
                  (recur (inc i)
                         :en-guion
                         inicio-tok
                         (.append buf ch)
                         tokens
                         (conj errores {:fase fase
                                        :tipo :guion-consecutivo
                                        :mensaje (str "Guiones consecutivos no permitidos en la posición " i ".")}))

                  :en-palabra
                  (recur (inc i) :en-guion inicio-tok (.append buf ch) tokens errores))

                :else
                (if (= estado :inicio)
                  (recur (inc i) :en-palabra i (.append (StringBuilder.) ch) tokens errores)
                  (recur (inc i) :en-palabra inicio-tok (.append buf ch) tokens errores))))))))))
