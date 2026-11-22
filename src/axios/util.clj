(ns axios.util
  "Funciones pequeñas que usan varias fases.")

(defn ahora
  "Fecha y hora actual como texto ISO, ej. \"2026-11-21T16:40:00\"."
  []
  (str (.withNano (java.time.LocalDateTime/now) 0)))
