(ns axios.normalizador
  "Normalizador del español (PP-12).
  Contrato: (normalizar texto) → {:original ... :texto ... :pregunta? ...}
  Responsable: Luisa (E1)."
  (:require [clojure.string :as str]))

(def ^:private mapa-tildes
  {\á \a \é \e \í \i \ó \o \ú \u \ü \u
   \Á \a \É \e \Í \i \Ó \o \Ú \u \Ü \u
   \ñ \n \Ñ \n})

(defn- quitar-tildes [s]
  (apply str (map #(get mapa-tildes % %) s)))

(def ^:private unidades
  {"cero" 0 "uno" 1 "dos" 2 "tres" 3 "cuatro" 4 "cinco" 5 "seis" 6 "siete" 7 "ocho" 8 "nueve" 9
   "diez" 10 "once" 11 "doce" 12 "trece" 13 "catorce" 14 "quince" 15 "dieciseis" 16 "diecisiete" 17 "dieciocho" 18 "diecinueve" 19
   "veinte" 20 "veintiuno" 21 "veintidos" 22 "veintitres" 23 "veinticuatro" 24 "veinticinco" 25 "veintiseis" 26 "veintisiete" 27 "veintiocho" 28 "veintinueve" 29})

(def ^:private decenas
  {"treinta" 30 "cuarenta" 40 "cincuenta" 50 "sesenta" 60 "setenta" 70 "ochenta" 80 "noventa" 90})

(def ^:private centenas
  {"cien" 100 "ciento" 100 "doscientos" 200 "trescientos" 300 "cuatrocientos" 400 "quinientos" 500 "seiscientos" 600 "setecientos" 700 "ochocientos" 800 "novecientos" 900})

(defn- valor-de-palabra [palabra]
  (or (get unidades palabra) (get decenas palabra) (get centenas palabra)))

(defn- numero-en-letras->valor [palabras]
  (loop [palabras palabras total 0 actual 0]
    (if-let [p (first palabras)]
      (cond
        (= p "mil") 
        ;; CORRECCIÓN: Si actual es 0, "mil" vale 1000. Si es 2, "dos mil" vale 2000.
        (recur (rest palabras) (+ total (* 1000 (if (zero? actual) 1 actual))) 0)
        (= p "y") (recur (rest palabras) total actual)
        :else (if-let [v (valor-de-palabra p)]
                (recur (rest palabras) total (+ actual v))
                (+ total actual)))
      (+ total actual))))

(defn- reemplazar-numeros-en-texto [texto]
  (let [tokens (str/split texto #"\s+" -1)
        numerico? (fn [t] (or (contains? unidades t) (contains? decenas t) (contains? centenas t) (= t "mil") (= t "y")))]
    (loop [tokens-rest tokens resultado [] acumulado []]
      (if (empty? tokens-rest)
        ;; Fin del texto
        (if (empty? acumulado)
          (str/join " " resultado)
          (if (= acumulado ["y"])
            (str/join " " (conj resultado "y"))  ;; CORRECCIÓN: Si es solo "y", se agrega
            (let [v (numero-en-letras->valor acumulado)]
              (str/join " " (conj resultado (str v))))))
        (let [t (first tokens-rest)]
          (if (numerico? t)
            (recur (rest tokens-rest) resultado (conj acumulado t))
            ;; No numérico: vaciar acumulado
            (let [resultado2 (if (empty? acumulado)
                               resultado
                               (if (= acumulado ["y"])
                                 (conj resultado "y")  ;; CORRECCIÓN: Si es solo "y", se agrega
                                 (let [v (numero-en-letras->valor acumulado)]
                                   (conj resultado (str v)))))]
              (recur (rest tokens-rest) (conj resultado2 t) []))))))))

(def ^:private interrogativos
  #{"que" "cuando" "donde" "cuantos" "cuantas" "quien" "quienes" "como" "cual" "cuales"})

(defn- es-pregunta? [texto-original texto-normalizado]
  (let [tiene-signo (or (str/includes? texto-original "¿") (str/includes? texto-original "?"))
        primera-palabra (first (str/split (str/trim texto-normalizado) #"\s+"))]
    (boolean (or tiene-signo (contains? interrogativos primera-palabra)))))

(defn normalizar [texto]
  ;; CORRECCIÓN: Solo verificar el ʔ (Ozh), no la ü, para no tumbar palabras como "vergüenza"
  (if (str/includes? texto "ʔ")
    (throw (ex-info "Texto en Ozh, no se procesa en español" {:fase :normalizador :tipo :texto-ozh}))
    (let [minusculas  (str/lower-case texto)
          sin-tildes  (quitar-tildes minusculas)
          con-numeros (reemplazar-numeros-en-texto sin-tildes)
          pregunta?   (es-pregunta? texto con-numeros)]
      {:original  texto
       :texto     con-numeros
       :pregunta? pregunta?})))