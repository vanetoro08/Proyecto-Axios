(ns axios.corrector
  "Fase 3. Corrector con distancia de Levenshtein propia (contratos.md, sección 3).
  Responsable: Luisa (PP-14 vocabulario, PP-15 corrector).

  Regla de oro: nunca cambia una palabra que ya está en el vocabulario.")

;; ---------------------------------------------------------------------------
;; STUB — no corrige nada: devuelve los tokens tal cual.
;; Reemplazar por la implementación real en PP-15.
;; ---------------------------------------------------------------------------

(defn levenshtein
  "Distancia de edición entre dos palabras. TODO PP-15."
  [_a _b]
  0)

(defn corregir
  "Recibe tokens y el conjunto de palabras españolas conocidas.
  Devuelve {:tokens [...] :correcciones [{:original :corregida :distancia :pos}]}"
  [tokens _vocabulario]
  {:tokens       tokens
   :correcciones []})
