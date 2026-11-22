(ns axios.validador.ozh
  "Validador de Ozh: analizador léxico y sintáctico propio, independiente
  del generador (contratos.md, sección 7). Condición 2 de Axios.
  Responsable: Jhojan (PP-26).

  Recibe SOLO el texto y lo vuelve a leer. Maneja los símbolos ʔ y ü.
  Rechaza oraciones sin partícula. Acepta el pasado negado sin ʔoka (D-012)
  y el prefijo ʔa- delante de un verbo que ya empieza por ʔ (ʔaʔim, D-008). Las reglas se leen de la BD.")

;; ---------------------------------------------------------------------------
;; STUB — aprueba todo. Reemplazar por el validador real en PP-26.
;; ---------------------------------------------------------------------------

(defn validar
  "Devuelve {:valida? true|false :errores [{:fase :tipo :mensaje}]}"
  [_texto]
  {:valida? true
   :errores []})
