(ns axios.generador.numeros
  "Conversión de cantidades a Kethari y Ozh (hasta 9.999).
  Responsable: Santiago (PP-20).

  Ozh: base 10, cifras encadenadas con guiones (15 → \"ois-ulo\").
  Kethari: hipótesis de base 6 (6 → \"ti-vo\", 15 → \"ra-sel\"). Ver bitácora.
  Las palabras de las cifras se leen de la BD.")

;; ---------------------------------------------------------------------------
;; STUB — TODO PP-20.
;; ---------------------------------------------------------------------------

(defn en-kethari
  "Ej: (en-kethari 15) → \"ra-sel\""
  [_n]
  "ra-sel")

(defn en-ozh
  "Ej: (en-ozh 15) → \"ois-ulo\""
  [_n]
  "ois-ulo")
