(ns axios.cargar-test
  "Pruebas de la orden `cargar` (PP-30, bitácora D-002). Responsable: Vanessa.
  Cada prueba trabaja en una BD temporal, para no tocar axios.db."
  (:require [axios.bd :as bd]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(def ^:private consultar @#'axios.bd/consultar)

(defn- bd-temporal [f]
  (let [archivo (java.io.File/createTempFile "axios-prueba" ".db")]
    (.delete archivo)
    (try
      (binding [bd/*ruta-bd* (.getPath archivo)]
        (f))
      (finally (.delete archivo)))))

(use-fixtures :each bd-temporal)

(defn- contar [tabla]
  (:n (first (consultar (str "SELECT COUNT(*) AS n FROM " tabla)))))

(defn- archivo-temporal [contenido]
  (let [f (java.io.File/createTempFile "senales" ".txt")]
    (.deleteOnExit f)
    (spit f contenido :encoding "UTF-8")
    (.getPath f)))

(deftest la-semana-1-viene-con-la-bd-y-la-semana-2-entra-con-cargar
  (testing "al crear la BD ya están las 15 señales de la semana 1 (D-025)"
    (is (= 15 (contar "senal")))
    (is (= [{:tipo "senal" :descripcion "Señales de senales_semana1.txt (manual 0.7)" :fecha "2026-11-10"}]
           (consultar "SELECT tipo, descripcion, fecha FROM fuente WHERE id > 8 ORDER BY id"))))
  (testing "cargar la semana 1 otra vez no repite nada"
    (is (= {:archivo "data/senales_semana1.txt" :version "0.7" :senales 0
            :correcciones 0 :repetidas 15 :errores []}
           (bd/cargar-archivo! "data/senales_semana1.txt" "2026-11-21"))))
  (testing "semana 2: 6 señales y 1 corrección, manual 0.8"
    (is (= {:archivo "data/senales_semana2.txt" :version "0.8" :senales 6
            :correcciones 1 :repetidas 0 :errores []}
           (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-23"))))
  (testing "quedan 21 señales (D-002)"
    (is (= 21 (contar "senal"))))
  (testing "la señal del día 6 sin transcripción queda guardada con transcripción vacía"
    (is (= [{:transcripcion nil :salida "Ya los escuchamos."}]
           (consultar "SELECT transcripcion, salida FROM senal WHERE voz = 'desconocida'"))))
  (testing "cada carga queda con su fuente y su fecha"
    (is (= [{:tipo "senal" :descripcion "Señales de senales_semana1.txt (manual 0.7)" :fecha "2026-11-10"}
            {:tipo "senal" :descripcion "Señales de senales_semana2.txt (manual 0.8)" :fecha "2026-11-23"}
            {:tipo "correccion" :descripcion "Correcciones de senales_semana2.txt (manual 0.8)" :fecha "2026-11-23"}]
           (consultar "SELECT tipo, descripcion, fecha FROM fuente WHERE id > 8 ORDER BY id")))))

(deftest la-correccion-de-teka-d005
  (testing "antes de la semana 2, teka es tierra"
    (is (= "teka" (:forma (bd/palabra-en :tierra :kethari))))
    (is (nil? (bd/palabra-en :territorio-reclamado :kethari))))
  (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-23")
  (testing "después, teka es territorio reclamado y tierra se queda sin palabra en Kethari"
    (is (= "teka" (:forma (bd/palabra-en :territorio-reclamado :kethari))))
    (is (nil? (bd/palabra-en :tierra :kethari))))
  (testing "en Ozh no cambia nada: tierra sigue siendo mak"
    (is (= "mak" (:forma (bd/palabra-en :tierra :ozh)))))
  (testing "el significado anterior no se borra: queda con su vigencia cerrada"
    (is (= [{:clave "tierra" :vigente-desde "2026-11-10" :vigente-hasta "2026-11-23"}
            {:clave "territorio-reclamado" :vigente-desde "2026-11-23" :vigente-hasta nil}]
           (consultar "SELECT c.clave, p.vigente_desde, p.vigente_hasta
                       FROM palabra p JOIN concepto c ON c.id = p.concepto_id
                       WHERE p.lengua = 'kethari' AND p.forma = 'teka' ORDER BY p.id")))))

(deftest cargar-dos-veces-no-repite-nada
  (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-23")
  (let [otra-vez (bd/cargar-archivo! "data/senales_semana2.txt" "2026-11-24")]
    (is (= 0 (:senales otra-vez)))
    (is (= 0 (:correcciones otra-vez)))
    (is (= 7 (:repetidas otra-vez)))
    (is (= 21 (contar "senal")))
    (is (= 2 (:n (first (consultar "SELECT COUNT(*) AS n FROM palabra
                                    WHERE lengua = 'kethari' AND forma = 'teka'")))))))

(deftest las-lineas-malas-se-reportan-y-la-carga-sigue
  (let [ruta (archivo-temporal
              (str "# prueba · Axios · manual versión 0.9\n"
                   "SENAL|11|kethari|anul parn thal|Nosotros queremos paz.\n"
                   "MENSAJE|algo|que|no|se conoce\n"
                   "SENAL|12|ozh|faltan campos\n"
                   "SENAL|doce|ozh|lom tho oruvi ilu|Ellos quieren paz.\n"
                   "SENAL|13|marciano|x|y\n"
                   "CORRECCION|kethari|vael|agua|mar\n"
                   "\n"
                   "SENAL|14|ozh||Sin transcripción.\n"))
        r (bd/cargar-archivo! ruta "2026-11-24")]
    (is (= "0.9" (:version r)))
    (is (= 2 (:senales r)))
    (is (= [{:linea 3 :motivo "tipo desconocido 'MENSAJE'"}
            {:linea 4 :motivo "se esperaban 5 campos y hay 4"}
            {:linea 5 :motivo "el día no es un número"}
            {:linea 6 :motivo "voz desconocida 'marciano' (se espera kethari, ozh o desconocida)"}
            {:linea 7 :motivo "'vael' no significa 'agua' en kethari según la BD"}]
           (:errores r)))))

(deftest un-significado-nuevo-crea-su-concepto
  (let [ruta (archivo-temporal "CORRECCION|ozh|mak|tierra|suelo fértil\n")
        r (bd/cargar-archivo! ruta "2026-11-24")]
    (is (= 1 (:correcciones r)))
    (is (= "mak" (:forma (bd/palabra-en :suelo-fertil :ozh))))
    (is (= {:concepto :suelo-fertil :categoria :sustantivo} (bd/buscar-es "suelo fertil")))
    (is (nil? (bd/palabra-en :tierra :ozh)))))

(deftest archivo-que-no-existe
  (let [e (try (bd/cargar-archivo! "data/no-existe.txt") nil
               (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= :archivo-no-encontrado (:tipo e)))))

(deftest la-carga-es-todo-o-nada
  (testing "si algo falla a mitad de la carga, no queda nada a medias"
    (let [ruta (archivo-temporal "SENAL|20|ozh|a|b\nSENAL|21|ozh|c|d\n")
          antes (contar "senal")]
      (let [original @#'axios.bd/cargar-senal!
            llamadas (atom 0)]
        (with-redefs-fn {#'axios.bd/cargar-senal!
                         (fn [& args]
                           (if (= 2 (swap! llamadas inc))
                             (throw (Exception. "falla simulada"))
                             (apply original args)))}
          #(is (thrown? Exception (bd/cargar-archivo! ruta "2026-11-24")))))
      (is (= antes (contar "senal"))))))
