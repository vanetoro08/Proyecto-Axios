(ns axios.validador.kethari
  "Validador de Kethari: analizador léxico y sintáctico propio, independiente
  del generador (contratos.md, sección 7). Condición 2 de Axios.
  Responsable: Jhojan (PP-25).

  Recibe SOLO el texto y lo vuelve a leer. Las reglas se leen de la BD.")

;; ---------------------------------------------------------------------------
;; STUB — aprueba todo. Reemplazar por el validador real en PP-25.
;; ---------------------------------------------------------------------------

(defn validar
  "Devuelve {:valida? true|false :errores [{:fase :tipo :mensaje}]}"
  [_texto]
  {:valida? true
   :errores []})
