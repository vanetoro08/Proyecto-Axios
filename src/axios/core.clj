(ns axios.core
  "Interfaz de línea de comandos (enunciado, sección 5.6).
  Responsable: Vanessa (PP-34 órdenes, PP-35 modo traza).

  Uso:
    clojure -M:run traducir \"<pregunta>\" [--traza]
    clojure -M:run cargar <archivo>
    clojure -M:run historial [desde <fecha>]
    clojure -M:run palabra <término> [en <fecha>]"
  (:require [axios.bd :as bd]
            [axios.pipeline :as pipeline])
  (:gen-class))

(def ^:private uso
  (str "Órdenes:\n"
       "  traducir \"<pregunta>\" [--traza]\n"
       "  cargar <archivo>\n"
       "  historial [desde <fecha>]\n"
       "  palabra <término> [en <fecha>]"))

;; ---------------------------------------------------------------------------
;; Modo traza (PP-35): la salida de cada fase, en el orden del pipeline
;; ---------------------------------------------------------------------------

(def ^:private fase-numero
  "En qué paso de la traza se detiene cada fase cuando falla."
  {:normalizador 1 :lexer 2 :corrector 3 :morfologia 4 :parser 5
   :generador 6 :validador-kethari 6 :validador-ozh 6 :pipeline 6})

(defn- persona-texto [persona numero]
  (str persona ".ª persona " (name numero)))

(defn- frase-texto
  "Una frase nominal del árbol, en palabras."
  [f]
  (case (:tipo f)
    nil         "(ninguno)"
    :pronombre  (str "pronombre de " (persona-texto (:persona f) (:numero f))
                     (when (:inferido? f) " (deducido del verbo)"))
    :sustantivo (str (name (:concepto f))
                     (when (:plural? f) ", plural")
                     (when (:cantidad f) (str ", cantidad " (:cantidad f)))
                     (when-let [p (:poseedor f)]
                       (str ", de la " (persona-texto (:persona p) (:numero p)))))))

(defn- token-texto
  "Lo que la morfología dijo de un token, en una línea."
  [t]
  (let [{:keys [categoria verbo concepto plural?]} t]
    (cond
      (= :numero (:tipo t)) (str "número " (:valor t))
      (= :signo (:tipo t))  "signo"
      (= :verbo categoria)  (str "verbo " (:infinitivo verbo) ", " (name (:tiempo verbo)) ", "
                                 (persona-texto (:persona verbo) (:numero verbo))
                                 (when (:perifrasis? verbo) " (ir a + infinitivo)")
                                 (when-let [a (:alternativa verbo)]
                                   (str " (también puede ser " (name (:tiempo a)) ", "
                                        (persona-texto (:persona a) (:numero a)) ")")))
      (= :sustantivo categoria) (str "sustantivo " (name concepto) (when plural? ", plural"))
      (= :desconocida categoria) "palabra desconocida"
      :else (str (name categoria) (when concepto (str " (" (name concepto) ")"))))))

(defn- titulo [n texto]
  (println (str "\n── " n ". " texto " ──")))

(defn- mostrar-traza
  "Muestra lo que hizo cada fase. Si una fase falló, lo dice y la traza se
  detiene ahí; el resultado final y la explicación se muestran después."
  [{:keys [original normalizado tokens-lexer correcciones tokens arbol traducciones error]}]
  (let [detenida (when error (fase-numero (:fase error) 6))
        mostrar? (fn [n] (or (nil? detenida) (<= n detenida)))
        fallo    (fn [n]
                   (when (= n detenida)
                     (println (str "  ✗ La pregunta se detuvo aquí (" (name (:tipo error)) ")."))))]
    (titulo 1 "Normalizador")
    (println "  Entrada:  " original)
    (when normalizado
      (println "  Salida:   " (:texto normalizado))
      (println "  Pregunta: " (if (:pregunta? normalizado) "sí" "no")))
    (fallo 1)

    (when (mostrar? 2)
      (titulo 2 "Analizador léxico (AFD)")
      (doseq [t tokens-lexer]
        (println (format "  %-12s %-8s posición %d–%d"
                         (:lexema t) (name (:tipo t)) (get-in t [:pos :inicio]) (get-in t [:pos :fin]))))
      (fallo 2))

    (when (mostrar? 3)
      (titulo 3 "Corrector (Levenshtein)")
      (if (empty? correcciones)
        (println "  Sin correcciones.")
        (doseq [{:keys [original corregida distancia]} correcciones]
          (println (str "  " original " → " corregida " (distancia " distancia ")"))))
      (fallo 3))

    (when (mostrar? 4)
      (titulo 4 "Morfología")
      (doseq [t tokens :when (not= :signo (:tipo t))]
        (println (format "  %-12s %s" (:lexema t) (token-texto t))))
      (fallo 4))

    (when (mostrar? 5)
      (titulo 5 "Analizador sintáctico (árbol intermedio)")
      (when arbol
        (println "  Tipo:          " (name (:tipo arbol)))
        (println "  Interrogativo: " (if-let [i (:interrogativo arbol)]
                                       (str (name (:concepto i)) " (" (name (:rol i)) ")")
                                       "(ninguno)"))
        (println "  Sujeto:        " (frase-texto (:sujeto arbol)))
        (let [v (:verbo arbol)]
          (println "  Verbo:         " (str (name (:concepto v)) ", " (name (:tiempo v))
                                            (when (:negado? v) ", negado"))))
        (println "  Objeto:        " (frase-texto (:objeto arbol))))
      (fallo 5))

    (when (mostrar? 6)
      (titulo 6 "Generadores y validadores")
      (doseq [[lengua nombre] [[:kethari "Kethari"] [:ozh "Ozh"]]
              :let [{:keys [texto palabras valida? errores]} (get traducciones lengua)]
              :when texto]
        (println (str "  " nombre ": " texto))
        (println (str "    Palabras:  "
                      (apply str (interpose ", " (for [p palabras]
                                                   (str (:forma p)
                                                        (when-let [c (:concepto p)] (str " (" (name c) ")"))
                                                        " [fuente " (:fuente-id p) "]"))))))
        (println (str "    Validador: " (if valida?
                                           "la reconoce como válida"
                                           (str "no la reconoce: "
                                                (apply str (interpose "; " (map :mensaje errores))))))))
      (fallo 6))
    (println)))

(defn- mostrar-resultado [r]
  (println "Pregunta:" (:original r))
  (if (= :aprobada (:estado r))
    (do (println "Kethari: " (get-in r [:traducciones :kethari :texto]))
        (println "Ozh:     " (get-in r [:traducciones :ozh :texto])))
    (println "No se transmite:" (:explicacion r)))
  (doseq [aviso (:avisos r)]
    (println "Aviso:" aviso))
  (println "Registrada con id" (:id r)))

(defn- mostrar-carga
  "Orden cargar (PP-30): resumen de lo que entró y de las líneas que no se entendieron."
  [ruta]
  (if (nil? ruta)
    (println "Falta el archivo. Uso: cargar <archivo>")
    (try
      (let [{:keys [version senales correcciones repetidas errores]} (bd/cargar-archivo! ruta)]
        (println (str "Archivo: " ruta (when version (str " (manual " version ")"))))
        (println "Señales nuevas:      " senales)
        (println "Correcciones nuevas: " correcciones)
        (when (pos? repetidas)
          (println "Ya estaban cargadas: " repetidas))
        (doseq [{:keys [linea motivo]} errores]
          (println (str "Línea " linea " no se cargó: " motivo))))
      (catch clojure.lang.ExceptionInfo e
        (println (ex-message e))))))

(defn- mostrar-historial
  "Orden historial: cada pregunta con su estado y su traducción o su explicación."
  [desde]
  (let [preguntas (bd/historial desde)]
    (if (empty? preguntas)
      (println (if desde (str "No hay preguntas desde " desde ".") "Todavía no hay preguntas registradas."))
      (doseq [r preguntas]
        (println (str "#" (:id r) "  " (:fecha r) "  " (name (:estado r))))
        (println "  Pregunta:" (:original r))
        (if (= :aprobada (:estado r))
          (do (println "  Kethari: " (get-in r [:traducciones :kethari :texto]))
              (println "  Ozh:     " (get-in r [:traducciones :ozh :texto])))
          (println "  No se transmitió:" (:explicacion r)))
        (doseq [aviso (:avisos r)]
          (println "  Aviso:" aviso))))))

(def ^:private nombre-lengua {:es "Español" :kethari "Kethari" :ozh "Ozh"})

(defn- vigencia [{:keys [desde hasta]}]
  (str "desde " desde (when hasta (str " hasta " hasta))))

(defn- mostrar-palabra
  "Orden palabra (PP-33): cada significado del término, con su vigencia, su
  fuente y sus equivalentes en las otras lenguas."
  [termino fecha]
  (if (nil? termino)
    (println "Falta el término. Uso: palabra <término> [en <fecha>]")
    (try
      (let [{:keys [significados] :as info} (bd/palabra-info termino fecha)]
        (if (empty? significados)
          (println (str "'" (:termino info) "' no está en la base de datos"
                        (when fecha (str " con vigencia el " fecha)) "."))
          (doseq [{:keys [lengua significado fuente equivalentes] :as s} significados]
            (println (str (:termino info) " (" (nombre-lengua lengua) ") = \"" significado "\""))
            (println (str "  Vigente " (vigencia s) ". Fuente: " fuente))
            (doseq [[l vs] equivalentes]
              (println (str "  " (nombre-lengua l) ": "
                            (apply str (interpose ", " (for [v vs]
                                                         (str (:forma v)
                                                              (when (:hasta v) (str " (" (vigencia v) ")"))))))))))))
      (catch clojure.lang.ExceptionInfo e
        (println (ex-message e))))))

(defn ejecutar
  "Interpreta los argumentos y ejecuta la orden. Separado de -main para poder probarlo."
  [args]
  (let [[orden & resto] args]
    (case orden
      "traducir"  (let [[pregunta & opciones] resto
                        r (pipeline/traducir pregunta)]
                    (when (some #{"--traza"} opciones) (mostrar-traza r))
                    (mostrar-resultado r))
      "cargar"    (mostrar-carga (first resto))
      "historial" (let [[_ fecha] resto]               ; historial desde 2026-11-21
                    (mostrar-historial fecha))
      "palabra"   (let [[termino _ fecha] resto]       ; palabra vael en 2026-11-21
                    (mostrar-palabra termino fecha))
      (println uso))))

(defn -main [& args]
  (ejecutar args))
