(ns axios.normalizador
  "Normalizador del español (PP-12).
  Contrato: (normalizar texto) → {:original ... :texto ... :pregunta? ...}
  Responsable: Luisa (E1).
  Única fase donde se permite regex (solo para separar por espacios)."
  (:require [clojure.string :as str]
            [axios.bd :as bd]
            [axios.errores :as errores]))

;; ---------------------------------------------------------------------------
;; 1. Tildes y ñ (D-003)
;; ---------------------------------------------------------------------------

(def ^:private mapa-tildes
  {\á \a \é \e \í \i \ó \o \ú \u \ü \u
   \Á \a \É \e \Í \i \Ó \o \Ú \u \Ü \u
   \ñ \n \Ñ \n})

(defn- quitar-tildes [s]
  (apply str (map #(get mapa-tildes % %) s)))

;; ---------------------------------------------------------------------------
;; 2. Números en letras → cifras (hasta 9.999)
;; ---------------------------------------------------------------------------

(def ^:private unidades
  {"cero" 0 "uno" 1 "dos" 2 "tres" 3 "cuatro" 4 "cinco" 5 "seis" 6 "siete" 7 "ocho" 8 "nueve" 9
   "diez" 10 "once" 11 "doce" 12 "trece" 13 "catorce" 14 "quince" 15 "dieciseis" 16 "diecisiete" 17
   "dieciocho" 18 "diecinueve" 19 "veinte" 20 "veintiuno" 21 "veintidos" 22 "veintitres" 23
   "veinticuatro" 24 "veinticinco" 25 "veintiseis" 26 "veintisiete" 27 "veintiocho" 28 "veintinueve" 29})

(def ^:private decenas
  {"treinta" 30 "cuarenta" 40 "cincuenta" 50 "sesenta" 60 "setenta" 70 "ochenta" 80 "noventa" 90})

(def ^:private centenas
  {"cien" 100 "ciento" 100 "doscientos" 200 "trescientos" 300 "cuatrocientos" 400 "quinientos" 500
   "seiscientos" 600 "setecientos" 700 "ochocientos" 800 "novecientos" 900})

(def ^:private maximo 9999)

(defn- valor-de-palabra [palabra]
  (or (get unidades palabra) (get decenas palabra) (get centenas palabra)))

(defn- numerico? [palabra]
  (boolean (or (valor-de-palabra palabra) (= palabra "mil") (= palabra "y"))))

(defn- numero-en-letras->valor
  "\"dos mil quince\" → 2015. \"mil\" solo vale 1000."
  [palabras]
  (loop [palabras palabras total 0 actual 0]
    (if-let [p (first palabras)]
      (cond
        (= p "mil") (recur (rest palabras) (+ total (* 1000 (if (zero? actual) 1 actual))) 0)
        (= p "y")   (recur (rest palabras) total actual)
        :else       (recur (rest palabras) total (+ actual (valor-de-palabra p))))
      (+ total actual))))

;; Los signos pueden ir pegados a una palabra: "¿quince", "quince." o "quince?".
;; Se separan para reconocer el número y se vuelven a pegar igual.
(def ^:private signos-apertura #{\¿ \¡})
(def ^:private signos-cierre #{\? \! \. \,})

(defn- partir-signos
  "\"¿quince?\" → [\"¿\" \"quince\" \"?\"]"
  [t]
  (let [pre    (apply str (take-while signos-apertura t))
        resto  (subs t (count pre))
        n-suf  (count (take-while signos-cierre (reverse resto)))
        nucleo (subs resto 0 (- (count resto) n-suf))]
    [pre nucleo (subs resto (count nucleo))]))

(defn- grupo->palabras
  "Convierte un grupo de palabras numéricas en cifras. Una \"y\" al inicio o al
  final del grupo no es parte del número (\"paz y agua\", \"agua y dos naves\")."
  [{:keys [pre nucleos suf]}]
  (let [inicio (take-while #{"y"} nucleos)
        resto  (drop-while #{"y"} nucleos)
        final  (take-while #{"y"} (reverse resto))
        medio  (drop-last (count final) resto)
        valor  (when (seq medio) (numero-en-letras->valor medio))]
    (when (and valor (> valor maximo))
      (errores/error-de-fase :normalizador :numero-fuera-de-rango
                             (str "Solo se aceptan cantidades hasta 9.999 (llegó " valor ").")
                             {:valor valor}))
    (let [palabras (vec (concat inicio (when valor [(str valor)]) final))]
      (-> palabras
          (update 0 #(str pre %))
          (update (dec (count palabras)) #(str % suf))))))

(defn- reemplazar-numeros-en-texto [texto]
  (let [partes (map partir-signos (remove str/blank? (str/split texto #"\s+")))
        cerrar (fn [res grupo] (if grupo (into res (grupo->palabras grupo)) res))]
    (loop [partes partes, res [], grupo nil]
      (if-let [[pre nucleo suf] (first partes)]
        (if (numerico? nucleo)
          ;; Sigue el mismo número si no hay un signo entre las dos palabras
          (if (and grupo (str/blank? pre) (str/blank? (:suf grupo)))
            (recur (rest partes) res (-> grupo (update :nucleos conj nucleo) (assoc :suf suf)))
            (recur (rest partes) (cerrar res grupo) {:pre pre :nucleos [nucleo] :suf suf}))
          (recur (rest partes) (conj (cerrar res grupo) (str pre nucleo suf)) nil))
        (str/join " " (cerrar res grupo))))))

;; ---------------------------------------------------------------------------
;; 3. ¿Es pregunta? (D-006)
;; ---------------------------------------------------------------------------

(defn- es-pregunta?
  "Con ¿ o ?, o si la primera palabra es interrogativa según la BD."
  [texto-normalizado]
  (let [primera (some-> texto-normalizado str/trim (str/split #"\s+") first partir-signos second)]
    (boolean
     (or (str/includes? texto-normalizado "¿")
         (str/includes? texto-normalizado "?")
         (and (seq primera)
              (= :interrogativo (:categoria (bd/buscar-es primera))))))))

;; ---------------------------------------------------------------------------
;; 4. Punto de entrada
;; ---------------------------------------------------------------------------

(defn normalizar [texto]
  (cond
    (str/blank? texto)
    (errores/error-de-fase :normalizador :entrada-vacia "La pregunta está vacía.")

    (str/includes? texto "ʔ")
    (errores/error-de-fase :normalizador :texto-ozh
                           "El texto parece estar en Ozh; el sistema solo traduce desde el español.")

    :else
    (let [texto-norm (-> texto str/lower-case quitar-tildes reemplazar-numeros-en-texto)]
      {:original  texto
       :texto     texto-norm
       :pregunta? (es-pregunta? texto-norm)})))
