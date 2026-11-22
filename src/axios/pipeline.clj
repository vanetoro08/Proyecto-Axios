(ns axios.pipeline
  "Orquestador: conecta todas las fases (contratos.md, sección 9).
  Responsable: todos (PP-36 integración; lidera Nicolas).

  Garantiza las condiciones de Axios:
  1. Ninguna pregunta se pierde: siempre se registra, con traducción o explicación.
  2. No sale nada sin validar: :aprobada solo si las dos traducciones son válidas.
  3. No se inventa nada: cada palabra generada lleva su :fuente-id."
  (:require [axios.bd :as bd]
            [axios.corrector :as corrector]
            [axios.errores :as errores]
            [axios.generador.kethari :as gen-kethari]
            [axios.generador.ozh :as gen-ozh]
            [axios.lexer :as lexer]
            [axios.morfologia :as morfologia]
            [axios.normalizador :as normalizador]
            [axios.parser :as parser]
            [axios.util :as util]
            [axios.validador.kethari :as val-kethari]
            [axios.validador.ozh :as val-ozh]))

(defn- traducir-y-validar
  "Genera una lengua y la vuelve a leer con su validador."
  [generar validar arbol]
  (let [{:keys [texto palabras]} (generar arbol)
        {:keys [valida? errores]} (validar texto)]
    {:texto texto :palabras palabras :valida? valida? :errores errores}))

(defn- ejecutar-fases
  "Corre todas las fases y va guardando la salida de cada una en el resultado
  (eso es lo que muestra el modo traza)."
  [resultado]
  (let [normalizado (normalizador/normalizar (:original resultado))
        tokens      (lexer/tokenizar (:texto normalizado))
        corregido   (corrector/corregir tokens (bd/vocabulario-es))
        analizados  (morfologia/analizar (:tokens corregido))
        arbol       (parser/analizar analizados)
        kethari     (traducir-y-validar gen-kethari/generar val-kethari/validar arbol)
        ozh         (traducir-y-validar gen-ozh/generar val-ozh/validar arbol)
        ambas-ok?   (and (:valida? kethari) (:valida? ozh))]
    (assoc resultado
           :normalizado  normalizado
           :tokens       analizados
           :correcciones (:correcciones corregido)
           :arbol        arbol
           :traducciones {:kethari kethari :ozh ozh}
           :estado       (if ambas-ok? :aprobada :rechazada)
           ;; TODO PP-16 / PP-19: juntar los avisos que agreguen la morfología y el
           ;; parser (D-006, D-010, D-015). Por ahora ninguna fase los produce.
           :avisos       []
           :explicacion  (when-not ambas-ok?
                           "Una de las traducciones no pasó la validación; no se transmite ninguna."))))

(defn traducir
  "Traduce una pregunta en español a Kethari y Ozh y la registra SIEMPRE."
  [texto]
  (let [base      {:fecha (util/ahora) :original texto :avisos []}
        resultado (try
                    (ejecutar-fases base)
                    (catch clojure.lang.ExceptionInfo e
                      (assoc base
                             :estado      :rechazada
                             :error       (ex-data e)
                             :explicacion (errores/explicar (ex-data e) (ex-message e)))))
        id        (bd/registrar-pregunta! resultado)]
    (assoc resultado :id id)))
