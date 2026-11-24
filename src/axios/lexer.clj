(ns axios.lexer
  "Analizador Léxico (AFD hecho a mano) para el español (PP-13).
  Contrato: (tokenizar texto) → [{:tipo :palabra :lexema \"...\" :pos {:inicio 0 :fin 5}} ...]
  Responsable: Luisa (E1).
  Nota: Recibe el :texto del normalizador, por lo que ya viene en minúsculas y sin tildes."
  (:require [axios.errores :as errores]))

;; CORRECCIÓN: Sin regex, usando comparación de caracteres (ASCII)
(defn- es-letra? [c]
  (or (and (>= (int c) 97) (<= (int c) 122)) ; a-z
      (= c \ñ)))

(defn- es-digito? [c]
  (and (>= (int c) 48) (<= (int c) 57))) ; 0-9

(def ^:private signos #{\¿ \? \¡ \! \. \,})

(defn tokenizar [texto]
  (let [n (count texto)]
    (loop [i 0
           tokens []]
      (if (>= i n)
        tokens
        (let [c (nth texto i)]
          (cond
            ;; 1. Ignorar espacios en blanco
            (Character/isWhitespace c)
            (recur (inc i) tokens)

            ;; 2. Estado de Palabra (letras)
            (es-letra? c)
            (let [j (loop [j i] 
                      (if (and (< j n) (es-letra? (nth texto j))) 
                        (recur (inc j)) 
                        j))]
              (recur j (conj tokens {:tipo :palabra 
                                     :lexema (subs texto i j) 
                                     :pos {:inicio i :fin j}})))

            ;; 3. Estado de Número (dígitos)
            (es-digito? c)
            (let [j (loop [j i] 
                      (if (and (< j n) (es-digito? (nth texto j))) 
                        (recur (inc j)) 
                        j))
                  lexema (subs texto i j)]
              (recur j (conj tokens {:tipo :numero 
                                     :lexema lexema 
                                     :valor (Integer/parseInt lexema) 
                                     :pos {:inicio i :fin j}})))

            ;; 4. Estado de Signo de puntuación permitido
            (contains? signos c)
            (recur (inc i) (conj tokens {:tipo :signo 
                                         :lexema (str c) 
                                         :pos {:inicio i :fin (inc i)}}))

            ;; 5. Carácter inválido
            :else
            (throw (errores/error-de-fase :lexer :caracter-invalido 
                                          (str "Carácter inválido: '" c "'") 
                                          i))))))))