(ns axios.generador.kethari
  "Generador de Kethari (contratos.md, sección 7).
  Responsable: Santiago (PP-22).

  Orden SOV, sujeto siempre explícito, tiempo con sufijo (-ok pasado, -ime futuro),
  negación nu-, plural -esh, números antes del sustantivo, poseedor delante,
  preguntas con partícula ka (interrogativa al inicio), sin artículos.
  Las palabras y las reglas se leen de la BD.")

;; ---------------------------------------------------------------------------
;; STUB — devuelve siempre la traducción de \"¿Cuándo vendrán ustedes?\"
;; (señal del día 5). Reemplazar por el generador real en PP-22.
;; ---------------------------------------------------------------------------

(defn generar
  "Recibe el árbol intermedio y devuelve
  {:lengua :kethari :texto \"...\" :palabras [{:forma :concepto :fuente-id}]}"
  [_arbol]
  {:lengua   :kethari
   :texto    "kom ulesh vinime ka"
   :palabras [{:forma "kom"    :concepto :cuando   :fuente-id 1}
              {:forma "ulesh"  :concepto :ustedes  :fuente-id 1}
              {:forma "vinime" :concepto :venir    :fuente-id 1}
              {:forma "ka"     :concepto :pregunta :fuente-id 1}]})
