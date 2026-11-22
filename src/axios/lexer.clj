(ns axios.lexer
  "Fase 2. Analizador léxico: autómata finito determinista escrito a mano
  (contratos.md, sección 2).
  Responsable: Luisa (PP-13).

  Sin regex ni librerías: el AFD recorre el texto carácter por carácter.
  El diagrama de estados va en docs/memoria.md.")

;; ---------------------------------------------------------------------------
;; STUB — devuelve siempre los tokens de \"¿cuando vendran ustedes?\".
;; Reemplazar por el AFD real en PP-13.
;; ---------------------------------------------------------------------------

(defn tokenizar
  "Recibe el :texto del normalizador y devuelve un vector de tokens
  {:tipo :palabra|:numero|:signo :lexema \"...\" :pos {:inicio n :fin n}}.
  Los :numero llevan además :valor."
  [_texto]
  [{:tipo :signo   :lexema "¿"       :pos {:inicio 0  :fin 1}}
   {:tipo :palabra :lexema "cuando"  :pos {:inicio 1  :fin 7}}
   {:tipo :palabra :lexema "vendran" :pos {:inicio 8  :fin 15}}
   {:tipo :palabra :lexema "ustedes" :pos {:inicio 16 :fin 23}}
   {:tipo :signo   :lexema "?"       :pos {:inicio 23 :fin 24}}])
