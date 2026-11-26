# Expediente Axios

Traductor de preguntas en español a las dos lenguas de las señales AX-7: **Kethari** y **Ozh**.
Proyecto final de Procesadores de Lenguaje (UAM), escrito en **Clojure** y sin librerías de
traducción, de procesamiento de lenguaje ni generadores de analizadores.

## Requisitos

- Java 17 o superior
- [Clojure CLI](https://clojure.org/guides/install_clojure) (comando `clojure`)
- Git (lo usa el corredor de pruebas)

## Cómo ejecutar

```bash
# Traducir una pregunta
clojure -M:run traducir "¿Cuándo vendrán ustedes?"

# Ver la salida de cada fase (modo traza)
clojure -M:run traducir "¿Cuándo vendrán ustedes?" --traza

# Cargar un archivo de señales o una corrección del manual (sin recompilar).
# La semana 1 ya entra sola al crear la BD (bitácora D-025).
clojure -M:run cargar data/senales_semana2.txt

# Ver lo que se ha traducido
clojure -M:run historial
clojure -M:run historial desde 2026-11-21

# Ver lo que el sistema sabe de una palabra (hoy o en una fecha)
clojure -M:run palabra vael
clojure -M:run palabra vael en 2026-11-21
```

## Cómo correr las pruebas

```bash
clojure -X:test
```

> **Estado actual:** ya funcionan con su implementación real el normalizador, el lexer, el
> corrector, la morfología, el parser, los validadores y la base de datos. Los generadores de
> Kethari y Ozh y la conversión de números todavía son *stubs* (PP-20 a PP-23): por ahora
> cualquier pregunta aprobada da la traducción de "¿Cuándo vendrán ustedes?", con el formato
> acordado en los [contratos entre fases](https://docs.google.com/document/d/12ZKKw4-0qtqQQiHC3ghMe7ijk7TvFg-91X0kjEhXxqs/edit).

## Estructura

| Archivo | Qué hace | Responsable | Jira |
|---|---|---|---|
| `src/axios/normalizador.clj` | Minúsculas, tildes y ñ, signos, números en letras | Luisa | PP-12 |
| `src/axios/lexer.clj` | Autómata finito determinista, tokens con posición | Luisa | PP-13 |
| `src/axios/corrector.clj` | Levenshtein contra el vocabulario | Luisa | PP-14, PP-15 |
| `src/axios/morfologia.clj` | Raíz, persona, número y tiempo de los verbos | Luisa | PP-16 |
| `src/axios/parser.clj` | Parser descendente recursivo → árbol intermedio | Nicolas | PP-17 a PP-19 |
| `src/axios/generador/numeros.clj` | Números en Kethari y Ozh | Santiago | PP-20 |
| `src/axios/generador/kethari.clj` | Árbol → Kethari | Santiago | PP-21, PP-22 |
| `src/axios/generador/ozh.clj` | Árbol → Ozh | Santiago | PP-21, PP-23 |
| `src/axios/validador/kethari.clj` | Relee y valida Kethari | Jhojan | PP-25 |
| `src/axios/validador/ozh.clj` | Relee y valida Ozh | Jhojan | PP-26 |
| `src/axios/errores.clj` | Formato único de errores | Jhojan | PP-27, PP-28 |
| `src/axios/bd.clj` | SQLite: léxico, reglas, señales, historial, versiones | Vanessa | PP-29 a PP-33 |
| `src/axios/core.clj` | Línea de comandos y modo traza | Vanessa | PP-34, PP-35 |
| `src/axios/pipeline.clj` | Conecta todas las fases | Todos (lidera Nicolas) | PP-36 |
| `test/` | Pruebas automáticas y pares de prueba | Jhojan | PP-24 |
| `data/senales_semana1.txt` | Señales de la semana 1 (formato de Axios, D-002). Se cargan al crear la BD (D-025) | Vanessa | PP-41 |
| `data/senales_semana2.txt` | Señales de la semana 2 y la corrección de *teka*. Entran con `cargar` | — | — |

## Documentos

Los documentos del proyecto están en Google Docs:

- [Contratos entre fases](https://docs.google.com/document/d/12ZKKw4-0qtqQQiHC3ghMe7ijk7TvFg-91X0kjEhXxqs/edit): qué entrega cada fase a la siguiente (PP-9).
- [Bitácora de decisiones](https://docs.google.com/document/d/1lsEu1irdY4UQOmzy9O5XPX8r44hip66EEcX-PYUnFWY/edit): las decisiones fechadas, con sus pruebas (PP-39).
- [Memoria](https://docs.google.com/document/d/1ipZEPRhYu_g2Zcg28VQQTpGDncnheGfLw8TNmNKcn6g/edit): BNF, autómata, modelo de datos y análisis de errores (PP-38).

## Cómo trabajamos

Ver [`CONTRIBUTING.md`](CONTRIBUTING.md): una rama por tarea, la clave de Jira en cada commit
y entrada a `develop` por pull request.

## Uso de inteligencia artificial

Declarado en la [memoria](https://docs.google.com/document/d/1ipZEPRhYu_g2Zcg28VQQTpGDncnheGfLw8TNmNKcn6g/edit), sección 15, como pide el enunciado.
