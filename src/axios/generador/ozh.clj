(ns axios.generador.ozh
  "Generador de Ozh (contratos.md, sección 7).
  Responsable: Santiago (PP-23).

  Orden VSO, partícula obligatoria después del verbo (zi / mar / tho),
  pasado con ʔoka y futuro con venu al inicio, negación ne delante del verbo,
  sustantivos sin plural, preguntas con prefijo ʔa- (interrogativa al final),
  poseedor detrás, sin artículos.
  Partícula: D-008 (preguntas) y D-016 (afirmaciones). Pasado negado: D-012.
  Las palabras y las reglas se leen de la BD.")

;; ---------------------------------------------------------------------------
;; STUB — devuelve siempre la traducción de \"¿Cuándo vendrán ustedes?\".
;; Reemplazar por el generador real en PP-23.
;; ---------------------------------------------------------------------------

(defn generar
  "Recibe el árbol intermedio y devuelve
  {:lengua :ozh :texto \"...\" :palabras [{:forma :concepto :fuente-id}]}"
  [_arbol]
  {:lengua   :ozh
   :texto    "venu ʔasur mar shavi wem"
   :palabras [{:forma "venu"  :concepto :futuro  :fuente-id 1}
              {:forma "ʔasur" :concepto :venir   :fuente-id 1}
              {:forma "mar"   :concepto :particula-mar :fuente-id 1}
              {:forma "shavi" :concepto :ustedes :fuente-id 1}
              {:forma "wem"   :concepto :cuando  :fuente-id 1}]})
