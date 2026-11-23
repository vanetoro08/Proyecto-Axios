(ns axios.validador.ozh-test
  "Pruebas automáticas del Validador Ozh (PP-26) contra la BD SQLite real."
  (:require [axios.bd :as bd]
            [axios.validador.lexer :as lexer]
            [axios.validador.ozh :as vo]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(defn- tipos-error [res]
  (set (map :tipo (:errores res))))

(defn- leer-senales-archivo
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
  (let [ok (vo/validar "venu ʔasur mar shavi wem")
        bad (vo/validar "lom oruvi ilu")]
    (is (= {:valida? true :errores []} ok))
    (is (= {:valida? false
            :errores [{:fase :validador-ozh
                       :tipo :sin-particula
                       :mensaje "Falta la partícula después del verbo"}]}
           bad))))

(deftest todas-las-senales-ozh-de-semana1-y-semana2-son-validas
  (let [s1 (leer-senales-archivo "data/senales_semana1.txt" "ozh")
        s2 (leer-senales-archivo "data/senales_semana2.txt" "ozh")
        todas (concat s1 s2)]
    (is (= 7 (count s1)))
    (is (= 3 (count s2)))
    (doseq [{:keys [dia transcripcion]} todas]
      (let [res (vo/validar transcripcion)]
        (is (:valida? res)
            (str "Señal Ozh día " dia " debería ser válida ('" transcripcion "'): "
                 (:errores res)))))))

(deftest oraciones-ozh-validas-cubren-d008-d012-d013-d016
  (doseq [frase ["ʔoka dra zi ʔenka vesh"
                 "lom tho oruvi ilu"
                 "lom mar ʔenka ilu"
                 "venu sur tho ois-ulo gor"
                 "ne ʔim zi ʔenka gor"
                 "ʔoka ne ʔim zi ʔenka gor"
                 "ʔoka kesh zi oruvi thüm ʔenka"
                 "ʔanem tho shavi wis"
                 "ne lom tho oruvi ilu"
                 "ʔaʔim mar shavi gor"
                 "ʔasur mar ois-ulo gor shavi wem"
                 "ʔalom mar shavi ois gor"
                 "ʔoka ʔim zi ʔenka hev-hev-hev-hev ral"
                 "venu bo mar ʔenka dav-zu-ois-ulo drüm"
                 "ʔaha mar thüm shavi wor"
                 "ʔalom mar oruvi wu"
                 "ʔoka ʔasur mar gor wak"]]
    (let [res (vo/validar frase)]
      (is (:valida? res) (str "Debería ser válida: '" frase "' -> " (:errores res))))))

(deftest rechaza-frases-ozh-invalidas-con-diagnostico-preciso
  (testing "Entrada vacía"
    (is (contains? (tipos-error (vo/validar "")) :entrada-vacia)))

  (testing "Sin partícula evidencial obligatoria o mal ubicada"
    (is (contains? (tipos-error (vo/validar "lom oruvi ilu")) :sin-particula))
    (is (contains? (tipos-error (vo/validar "lom oruvi tho ilu")) :sin-particula))
    (is (contains? (tipos-error (vo/validar "lom zi tho oruvi ilu")) :particula-duplicada)))

  (testing "Orden SOV/SVO rechazado (Ozh es VSO)"
    (is (contains? (tipos-error (vo/validar "ʔen naʔ lom zi")) :orden-invalido))
    (is (contains? (tipos-error (vo/validar "ʔen lom zi naʔ")) :orden-invalido)))

  (testing "Marcador de tiempo mal ubicado o duplicado"
    (is (contains? (tipos-error (vo/validar "dra zi ʔenka vesh ʔoka")) :marcador-tiempo-mal-ubicado))
    (is (contains? (tipos-error (vo/validar "ʔoka venu sur tho shavi")) :marcador-tiempo-duplicado)))

  (testing "Negación pegada al verbo o mal ubicada"
    (is (contains? (tipos-error (vo/validar "nesur tho shavi")) :morfologia-invalida))
    (is (contains? (tipos-error (vo/validar "dra ne zi ʔenka vesh")) :sin-particula)))

  (testing "Sustantivos con plural no reconocido en Ozh"
    (is (contains? (tipos-error (vo/validar "venu sur tho ois-ulo goresh")) :palabra-desconocida))
    (is (contains? (tipos-error (vo/validar "lom mar ʔen thümesh")) :palabra-desconocida)))

  (testing "Preguntas sin prefijo ʔa o interrogativa fuera del final"
    (is (contains? (tipos-error (vo/validar "sur mar shavi wem")) :sin-prefijo-pregunta))
    (is (contains? (tipos-error (vo/validar "wem ʔasur mar shavi")) :interrogativo-mal-ubicado)))

  (testing "Poseedor delante del sustantivo rechazado en Ozh"
    (is (contains? (tipos-error (vo/validar "kesh zi ʔen sha thüm")) :orden-invalido)))

  (testing "Sin sujeto y números inválidos"
    (is (contains? (tipos-error (vo/validar "sur tho")) :sin-sujeto))
    (is (contains? (tipos-error (vo/validar "sur tho zu-ois gor")) :numero-invalido))
    (is (contains? (tipos-error (vo/validar "sur tho ois-zu-zu-zu-zu gor")) :numero-fuera-de-rango)))

  (testing "Palabra Kethari mezclada en Ozh"
    (is (contains? (tipos-error (vo/validar "lom mar ʔen parn")) :palabra-desconocida))))

(deftest reconoce-palabras-nuevas-ozh-en-bd-sin-modificar-codigo
  (is (not (:valida? (vo/validar "lom mar ʔen kora"))))
  (let [formas-originales (bd/formas :ozh)]
    (with-redefs [bd/formas (fn [_] (conj formas-originales "kora"))]
      (is (:valida? (vo/validar "lom mar ʔen kora"))))))
