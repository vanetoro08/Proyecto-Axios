(ns axios.core
  "Interfaz de línea de comandos (enunciado, sección 5.6).
  Responsable: Vanessa (PP-34 órdenes, PP-35 modo traza).

  Uso:
    clojure -M:run traducir \"<pregunta>\" [--traza]
    clojure -M:run cargar <archivo>
    clojure -M:run historial [desde <fecha>]
    clojure -M:run palabra <término> [en <fecha>]"
  (:require [axios.bd :as bd]
            [axios.pipeline :as pipeline]
            [clojure.pprint :as pp])
  (:gen-class))

(def ^:private uso
  (str "Órdenes:\n"
       "  traducir \"<pregunta>\" [--traza]\n"
       "  cargar <archivo>\n"
       "  historial [desde <fecha>]\n"
       "  palabra <término> [en <fecha>]"))

(defn- mostrar-traza
  "Muestra la salida de cada fase. TODO PP-35: presentación más clara."
  [r]
  (doseq [[titulo clave] [["Normalizador" :normalizado]
                          ["Tokens (lexer + corrector + morfología)" :tokens]
                          ["Correcciones" :correcciones]
                          ["Árbol intermedio" :arbol]
                          ["Traducciones y validación" :traducciones]]]
    (println (str "\n── " titulo " ──"))
    (pp/pprint (get r clave))))

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
                    (doseq [r (bd/historial fecha)]
                      (println (:id r) (:fecha r) (name (:estado r)) "-" (:original r))))
      "palabra"   (let [[termino _ fecha] resto]       ; palabra vael en 2026-11-21
                    (pp/pprint (bd/palabra-info termino fecha)))
      (println uso))))

(defn -main [& args]
  (ejecutar args))
