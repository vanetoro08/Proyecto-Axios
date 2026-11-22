(ns axios.morfologia
  "Fase 4. Análisis morfológico: categoría de cada palabra y análisis verbal
  (raíz, persona, número, tiempo), incluido \"ir a + infinitivo\" y \"territorio(s) reclamado(s)\" como un
  solo token (contratos.md, sección 4; bitácora D-005, D-015).
  Responsable: Luisa (PP-16).

  Las reglas (terminaciones e irregulares) se leen de la BD, no van en el código.")

;; ---------------------------------------------------------------------------
;; STUB — devuelve siempre el análisis de \"¿cuando vendran ustedes?\".
;; Reemplazar por la implementación real en PP-16.
;; ---------------------------------------------------------------------------

(defn analizar
  "Recibe los tokens corregidos y les agrega :categoria (y :verbo, :pronombre,
  :concepto según el caso). Junta \"ir a + infinitivo\" en un solo token verbal."
  [_tokens]
  [{:tipo :signo   :lexema "¿" :pos {:inicio 0 :fin 1}}
   {:tipo :palabra :lexema "cuando" :pos {:inicio 1 :fin 7}
    :categoria :interrogativo :concepto :cuando}
   {:tipo :palabra :lexema "vendran" :pos {:inicio 8 :fin 15}
    :categoria :verbo
    :verbo {:infinitivo "venir" :concepto :venir :persona 3 :numero :plural :tiempo :futuro}}
   {:tipo :palabra :lexema "ustedes" :pos {:inicio 16 :fin 23}
    :categoria :pronombre
    :pronombre {:persona 2 :numero :plural}}
   {:tipo :signo   :lexema "?" :pos {:inicio 23 :fin 24}}])
