(ns axios.parser
  "Fase 5. Analizador sintáctico descendente recursivo sobre la BNF de
  docs/memoria.md (contratos.md, secciones 5 y 6).
  Responsable: Nicolas (PP-17 BNF, PP-18 y PP-19 parser).

  Una función por cada no terminal de la BNF.
  Produce el árbol intermedio: sin nada de Kethari ni de Ozh.
  Sujeto omitido y \"su\": bitácora D-010. Sin signos: D-006. \"Un/una\": D-011.")

;; ---------------------------------------------------------------------------
;; STUB — devuelve siempre el árbol de \"¿Cuándo vendrán ustedes?\".
;; Reemplazar por el parser real en PP-18 / PP-19.
;; ---------------------------------------------------------------------------

(defn analizar
  "Recibe los tokens de la morfología y devuelve el árbol intermedio.
  Si no cumplen la gramática, lanza un error de fase :parser."
  [_tokens]
  {:tipo          :pregunta-abierta
   :interrogativo {:concepto :cuando :rol :tiempo}
   :sujeto        {:tipo :pronombre :persona 2 :numero :plural :inferido? false}
   :verbo         {:concepto :venir :tiempo :futuro :negado? false}
   :objeto        nil})
