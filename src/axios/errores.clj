(ns axios.errores
  "Formato único de errores entre fases (contratos.md, sección 8).
  Responsable: Jhojan (PP-28).")

(defn error-de-fase
  "Lanza un error con el formato acordado.
  Ej: (error-de-fase :parser :estructura-invalida \"Falta el verbo\" {:pos 12})"
  [fase tipo mensaje detalle]
  (throw (ex-info mensaje {:fase fase :tipo tipo :detalle detalle})))

(defn explicar
  "Convierte los datos de un error en una explicación legible para el usuario."
  [{:keys [fase tipo]} mensaje]
  ;; TODO PP-28: mensajes claros por tipo de error y por fase.
  (str "No se pudo traducir (fase " (name fase) ", " (name tipo) "): " mensaje))
