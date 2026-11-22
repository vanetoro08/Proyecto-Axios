(ns axios.validador.kethari-test
  "Pruebas automáticas del Validador Kethari (PP-25) contra la BD SQLite real."
  (:require [axios.bd :as bd]
            [axios.validador.kethari :as vk]
            [axios.validador.lexer :as lexer]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(defn- tipos-error [res]
  (set (map :tipo (:errores res))))

(defn- leer-senales-archivo
  "Lee las señales de una voz desde los archivos data/senales_semana*.txt sin regex."
  [ruta voz-buscada]
  (->> (str/split-lines (slurp ruta :encoding "UTF-8"))
       (remove #(or (str/blank? %) (str/starts-with? % "#")))
       (map #(lexer/dividir-por-caracter % \|))
       (filter #(and (= "SENAL" (first %))
                     (= voz-buscada (nth % 2))
                     (not (str/blank? (nth % 3)))))
       (map (fn [[_ dia voz trans salida]]
              {:dia dia :voz voz :transcripcion trans :salida salida}))))

(deftest formato-de-retorno-del-contrato
  (let [ok (vk/validar "kom ulesh vinime ka")
        bad (vk/validar "kom ulesh vinime")]
    (is (= {:valida? true :errores []} ok))
    (is (false? (:valida? bad)))
    (is (every? #(= :validador-kethari (:fase %)) (:errores bad)))
    (is (every? #(and (:tipo %) (string? (:mensaje %))) (:errores bad)))))

(deftest todas-las-senales-kethari-de-semana1-y-semana2-son-validas
  (let [s1 (leer-senales-archivo "data/senales_semana1.txt" "kethari")
        s2 (leer-senales-archivo "data/senales_semana2.txt" "kethari")
        todas (concat s1 s2)]
    (is (= 7 (count s1)))
    (is (= 3 (count s2)))
    (doseq [{:keys [dia transcripcion]} todas]
      (let [res (vk/validar transcripcion)]
        (is (:valida? res)
            (str "Señal Kethari día " dia " debería ser válida ('" transcripcion "'): "
                 (:errores res)))))))

(deftest oraciones-kethari-validas-cubren-d004-d007-d013-y-preguntas
  (doseq [frase ["an dahl thal"
                 "anul parn thal"
                 "anir ombraesh salok"
                 "anir ulesh nukorok"
                 "ra-sel kumesh vinok"
                 "anul ti-vo lunesh tepime"
                 "anir mok-ra sorinesh korok"
                 "iresh tekaesh nuthal"
                 "ir yorr mi ka"
                 "iresh anir vael dunok"
                 "iresh anul vael dunime"
                 "kom ulesh ra-sel kumesh vin ka"
                 "ulesh ti kum thal ka"
                 "anir vo kumesh korok"
                 "anir ti-ti-mok-ti-mok-sel sorinesh korok"
                 "sem ombra salok ka"
                 "sem ulesh rhu ka"
                 "kel iresh thal ka"
                 "dor ulesh vael mi ka"
                 "kai kumesh vinok ka"
                 "kai sorinesh ul kor ka"]]
    (let [res (vk/validar frase)]
      (is (:valida? res) (str "Debería ser válida: '" frase "' -> " (:errores res))))))

(deftest rechaza-frases-kethari-invalidas-con-diagnostico-preciso
  (testing "Entrada vacía y caracteres prohibidos"
    (is (contains? (tipos-error (vk/validar "   ")) :entrada-vacia))
    (is (contains? (tipos-error (vk/validar "ʔen dahl thal")) :caracter-invalido))
    (is (contains? (tipos-error (vk/validar "kom ulesh vinime ka?")) :caracter-invalido)))

  (testing "Palabra desconocida o de Ozh mezclada"
    (is (contains? (tipos-error (vk/validar "an gor thal")) :palabra-desconocida)))

  (testing "Orden SOV violado, sin sujeto o sin verbo"
    (is (contains? (tipos-error (vk/validar "an thal dahl")) :orden-invalido))
    (is (contains? (tipos-error (vk/validar "thal an dahl")) :orden-invalido))
    (is (contains? (tipos-error (vk/validar "vinok")) :sin-sujeto))
    (is (contains? (tipos-error (vk/validar "an dahl")) :sin-verbo)))

  (testing "Reglas de preguntas (ka e interrogativa)"
    (is (contains? (tipos-error (vk/validar "kom ulesh vinime")) :sin-particula-pregunta))
    (is (contains? (tipos-error (vk/validar "ul ka parn thal")) :posicion-invalida-ka))
    (is (contains? (tipos-error (vk/validar "ulesh kom vinime ka")) :interrogativo-mal-ubicado)))

  (testing "D-004: discordancia anir/anul con el tiempo verbal"
    (is (contains? (tipos-error (vk/validar "anul ombraesh salok")) :concordancia-nosotros-tiempo))
    (is (contains? (tipos-error (vk/validar "anir parn thal")) :concordancia-nosotros-tiempo))
    (is (contains? (tipos-error (vk/validar "iresh anul vael dunok")) :concordancia-nosotros-tiempo)))

  (testing "D-013: orden incorrecto número + poseedor + sustantivo"
    (is (contains? (tipos-error (vk/validar "kom ra-sel ulesh kumesh vin ka")) :orden-poseedor-numero)))

  (testing "D-019: concordancia número-plural y números base 6 inválidos"
    (is (contains? (tipos-error (vk/validar "ra-sel kum vinok")) :concordancia-plural))
    (is (contains? (tipos-error (vk/validar "ti kumesh vinok")) :concordancia-plural))
    (is (contains? (tipos-error (vk/validar "kumesh ra-sel vinok")) :orden-numero-sustantivo))
    (is (contains? (tipos-error (vk/validar "vo-ti kumesh vinok")) :numero-invalido))
    (is (contains? (tipos-error (vk/validar "ti-sab kumesh vinok")) :numero-invalido))
    (is (contains? (tipos-error (vk/validar "ti-ti-mok-ti-mok-mok kumesh vinok")) :numero-fuera-de-rango)))

  (testing "Morfología ilegal"
    (is (contains? (tipos-error (vk/validar "an dahl nunuthal")) :morfologia-invalida))
    (is (contains? (tipos-error (vk/validar "an dahl thalokime")) :morfologia-invalida))
    (is (contains? (tipos-error (vk/validar "an kumeshesh kor")) :morfologia-invalida))))

(deftest reconoce-palabras-nuevas-en-bd-sin-modificar-codigo
  (is (not (:valida? (vk/validar "anul zora thal"))))
  (let [formas-originales (bd/formas :kethari)]
    (with-redefs [bd/formas (fn [_] (conj formas-originales "zora"))]
      (is (:valida? (vk/validar "anul zora thal"))))))
