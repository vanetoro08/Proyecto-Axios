(ns axios.normalizador
  "Fase 1. Normalizador (contratos.md, sección 1).
  Responsable: Luisa (PP-12).

  Pasa a minúsculas, quita tildes y convierte la ñ en n (bitácora D-003), deja los signos y convierte
  números escritos en letras a cifras (hasta 9.999).
  Es la ÚNICA fase donde se permite usar expresiones regulares.")

;; ---------------------------------------------------------------------------
;; STUB — devuelve siempre el mismo ejemplo, con el formato del contrato.
;; Reemplazar por la implementación real en PP-12.
;; ---------------------------------------------------------------------------

(defn normalizar
  "Recibe el texto tal como llega y devuelve
  {:original \"...\" :texto \"...\" :pregunta? true|false}"
  [texto]
  {:original  texto
   :texto     "¿cuando vendran ustedes?"
   :pregunta? true})
