# Cómo trabajamos

## Ramas

- `main` — solo recibe el release final desde `develop` (PP-40). Nadie hace commits directos aquí.
- `develop` — la versión integrada. Solo entra código por **pull request**.
- `feature/PP-xx-descripcion-corta` — una rama por tarea de Jira. Ej: `feature/PP-13-afd`.

## Flujo para cada tarea

```bash
git checkout develop
git pull
git checkout -b feature/PP-13-afd

# ...trabajar, con commits pequeños y seguidos...
git add .
git commit -m "feat(PP-13): estados del AFD para palabras y números"

git push -u origin feature/PP-13-afd
# Abrir pull request hacia develop. Cuando se acepta, se borra la rama.
```

## Reglas

1. **Formato de commit: `tipo(PP-xx): descripción`** (Conventional Commits con la clave de Jira).
   La clave hace que Jira enlace el código con la tarea. Tipos:

   | Tipo | Cuándo | Ejemplo |
   | --- | --- | --- |
   | `feat` | Funcionalidad nueva | `feat(PP-13): estados del AFD para palabras y números` |
   | `fix` | Corrección de un error | `fix(PP-15): el corrector cambiaba palabras válidas` |
   | `test` | Pruebas o pares de prueba | `test(PP-24): pares de preguntas negadas` |
   | `docs` | Documentación (bitácora, memoria, README) | `docs(PP-39): decisión D-017` |
   | `refactor` | Reorganizar código sin cambiar lo que hace | `refactor(PP-18): una función por no terminal` |
   | `chore` | Configuración, dependencias, estructura | `chore(PP-8): esqueleto del proyecto` |

   Descripción en minúscula, en presente y corta.
2. **Commits pequeños y seguidos**, no uno gigante al final. El historial de Git se califica.
3. **No tocar el código de otra fase** sin avisar a su responsable.
4. **No cambiar `docs/contratos.md`** sin avisar en el grupo y anotarlo en la bitácora.
5. **Toda decisión que el enunciado no resuelve va a `docs/bitacora.md`** el mismo día, con su prueba.
6. **Nada de léxico ni de reglas en el código**: todo sale de la BD.
7. **Expresiones regulares solo en `normalizador.clj`.** Ojo con las que no parecen: además de
   `#"..."`, `re-find`, `re-matches` y `re-seq`, también cuentan `str/split` y `str/replace` con
   un patrón `#"..."`, y el `.split` de Java. Para cortar las líneas de señales en cada `|`,
   usen una función propia que recorra el texto (PP-30).
8. Antes de abrir el pull request, correr `clojure -X:test`.

## Restricciones del enunciado (revisar antes del release, PP-40)

- [ ] Sin librerías de traducción ni de procesamiento de lenguaje (NLTK, spaCy, Freeling, Apertium…)
- [ ] Sin generadores de analizadores (ANTLR, Instaparse, etc.)
- [ ] Expresiones regulares solo en el normalizador. Este comando no debe mostrar ningún otro archivo:
      `grep -rlE '#"|re-(find|matches|seq|pattern)|\.split' src | grep -v normalizador`
- [ ] No queda ningún stub: `grep -rn STUB src` no muestra nada
- [ ] Nada del léxico ni de las reglas escrito en el código
- [ ] Funciona sin conexión (sin APIs ni servicios en red)
- [ ] Escrito en Clojure (lenguaje asignado)