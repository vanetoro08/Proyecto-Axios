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
  "Corre todas las fases. Cada una deja su salida en `parcial` apenas termina,
  así que si una fase falla, el resultado conserva lo que hicieron las
  anteriores: eso lo muestra el modo traza (PP-35) y se guarda en el historial."
  [parcial]
  (let [anotar      (fn [clave valor] (vswap! parcial assoc clave valor) valor)
        normalizado (anotar :normalizado (normalizador/normalizar (:original @parcial)))
        tokens      (anotar :tokens-lexer (lexer/tokenizar (:texto normalizado)))
        corregido   (corrector/corregir tokens (bd/vocabulario-es))
        _           (anotar :correcciones (:correcciones corregido))
        analizados  (anotar :tokens (morfologia/analizar (:tokens corregido)))
        ;; Avisos de cómo se interpretó la pregunta: los de la morfología
        ;; (D-015) y los del parser (D-006, D-010, D-017, D-023).
        _           (anotar :avisos (vec (keep #(get-in % [:verbo :aviso]) analizados)))
        arbol       (anotar :arbol (parser/analizar analizados))
        _           (vswap! parcial update :avisos into (:avisos arbol))
        kethari     (traducir-y-validar gen-kethari/generar val-kethari/validar arbol)
        _           (vswap! parcial assoc-in [:traducciones :kethari] kethari)
        ozh         (traducir-y-validar gen-ozh/generar val-ozh/validar arbol)
        _           (vswap! parcial assoc-in [:traducciones :ozh] ozh)
        ambas-ok?   (and (:valida? kethari) (:valida? ozh))]
    (assoc @parcial
           :estado      (if ambas-ok? :aprobada :rechazada)
           :explicacion (when-not ambas-ok?
                          (errores/explicar-rechazo-validadores kethari ozh)))))

(defn traducir
  "Traduce una pregunta en español a Kethari y Ozh y la registra SIEMPRE,
  también si una fase falla o si hay un error inesperado (condición 1)."
  [texto]
  (let [parcial   (volatile! {:fecha (util/ahora) :original texto :avisos []})
        rechazar  (fn [error mensaje]
                    (assoc @parcial
                           :estado      :rechazada
                           :error       error
                           :explicacion (errores/explicar error mensaje)))
        resultado (try
                    (ejecutar-fases parcial)
                    (catch clojure.lang.ExceptionInfo e
                      (rechazar (ex-data e) (ex-message e)))
                    (catch Exception e
                      (let [error (errores/extraer-error e)]
                        (rechazar error (:mensaje error)))))
        id        (bd/registrar-pregunta! resultado)]
    (assoc resultado :id id)))
