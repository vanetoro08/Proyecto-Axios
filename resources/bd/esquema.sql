-- Esquema de la base de datos de Axios (PP-29).
-- Basado en docs/contratos.md, sección "Base de datos".
-- Se ejecuta cada vez que arranca el programa: IF NOT EXISTS evita que falle
-- si las tablas ya existen.
-- Cambio frente al contrato: fuente.tipo acepta también 'decision', para las
-- reglas que salen de la bitácora (D-004, D-007, D-008, D-013, D-016).

CREATE TABLE IF NOT EXISTS fuente (
  id          INTEGER PRIMARY KEY,
  tipo        TEXT NOT NULL CHECK (tipo IN ('manual','correccion','senal','decision')),
  descripcion TEXT NOT NULL,
  fecha       TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS concepto (
  id        INTEGER PRIMARY KEY,
  clave     TEXT NOT NULL UNIQUE,
  categoria TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS palabra (
  id             INTEGER PRIMARY KEY,
  concepto_id    INTEGER NOT NULL REFERENCES concepto(id),
  lengua         TEXT NOT NULL CHECK (lengua IN ('es','kethari','ozh')),
  forma          TEXT NOT NULL,
  fuente_id      INTEGER NOT NULL REFERENCES fuente(id),
  vigente_desde  TEXT NOT NULL,
  vigente_hasta  TEXT
);

CREATE TABLE IF NOT EXISTS regla (
  id            INTEGER PRIMARY KEY,
  lengua        TEXT NOT NULL CHECK (lengua IN ('kethari','ozh')),
  clave         TEXT NOT NULL,
  valor         TEXT NOT NULL,
  fuente_id     INTEGER NOT NULL REFERENCES fuente(id),
  vigente_desde TEXT NOT NULL,
  vigente_hasta TEXT
);

CREATE TABLE IF NOT EXISTS morfologia_es (
  id          INTEGER PRIMARY KEY,
  terminacion TEXT NOT NULL,
  conjugacion TEXT NOT NULL,
  tiempo      TEXT NOT NULL,
  persona     INTEGER NOT NULL,
  numero      TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS verbo_irregular (
  id         INTEGER PRIMARY KEY,
  forma      TEXT NOT NULL,
  infinitivo TEXT NOT NULL,
  tiempo     TEXT NOT NULL,
  persona    INTEGER NOT NULL,
  numero     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS senal (
  id            INTEGER PRIMARY KEY,
  dia           INTEGER NOT NULL,
  voz           TEXT NOT NULL,
  transcripcion TEXT,
  salida        TEXT NOT NULL,
  fuente_id     INTEGER NOT NULL REFERENCES fuente(id)
);

CREATE TABLE IF NOT EXISTS pregunta (
  id          INTEGER PRIMARY KEY,
  fecha       TEXT NOT NULL,
  original    TEXT NOT NULL,
  normalizado TEXT,
  arbol       TEXT,
  estado      TEXT NOT NULL CHECK (estado IN ('aprobada','rechazada')),
  explicacion TEXT
);

CREATE TABLE IF NOT EXISTS correccion (
  id          INTEGER PRIMARY KEY,
  pregunta_id INTEGER NOT NULL REFERENCES pregunta(id),
  original    TEXT NOT NULL,
  corregida   TEXT NOT NULL,
  distancia   INTEGER NOT NULL,
  pos_inicio  INTEGER NOT NULL,
  pos_fin     INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS traduccion (
  id          INTEGER PRIMARY KEY,
  pregunta_id INTEGER NOT NULL REFERENCES pregunta(id),
  lengua      TEXT NOT NULL CHECK (lengua IN ('kethari','ozh')),
  texto       TEXT,
  valida      INTEGER NOT NULL,
  errores     TEXT
);

CREATE TABLE IF NOT EXISTS traduccion_palabra (
  traduccion_id INTEGER NOT NULL REFERENCES traduccion(id),
  palabra_id    INTEGER NOT NULL REFERENCES palabra(id),
  orden         INTEGER NOT NULL
);
