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

## Restricciones del enunciado (revisar antes del release, PP-40)

- [ ] Sin librerías de traducción ni de procesamiento de lenguaje (NLTK, spaCy, Freeling, Apertium…)
- [ ] Sin generadores de analizadores (ANTLR, Instaparse, etc.)
- [ ] Expresiones regulares solo en el normalizador. Este comando no debe mostrar ningún otro archivo:
      `grep -rlE '#"|re-(find|matches|seq|pattern)|\.split' src | grep -v normalizador`
- [ ] No queda ningún stub: `grep -rn STUB src` no muestra nada
- [ ] Nada del léxico ni de las reglas escrito en el código
- [ ] Funciona sin conexión (sin APIs ni servicios en red)
- [ ] Escrito en Clojure (lenguaje asignado)
