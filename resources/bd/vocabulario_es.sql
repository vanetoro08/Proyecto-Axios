-- Vocabulario español extendido (PP-14).
-- Se carga DESPUÉS de manual_v07.sql.
-- Todo sin tildes ni ñ (D-003).
-- Fuente: vocabulario general del español para el corrector.
-- Fecha: 2026-11-23

-- Primero, insertamos la fuente
INSERT INTO fuente (id, tipo, descripcion, fecha) 
VALUES (7, 'espanol', 'Vocabulario español extendido PP-14', '2026-11-23');

-- ============================================================
-- CONCEPTOS NUEVOS (que no están en manual_v07.sql)
-- ============================================================

-- Artículos (categoría: determinante)
INSERT INTO concepto (clave, categoria) VALUES ('el-art', 'determinante');
INSERT INTO concepto (clave, categoria) VALUES ('la', 'determinante');
INSERT INTO concepto (clave, categoria) VALUES ('los', 'determinante');
INSERT INTO concepto (clave, categoria) VALUES ('las', 'determinante');
INSERT INTO concepto (clave, categoria) VALUES ('un', 'determinante');
INSERT INTO concepto (clave, categoria) VALUES ('una', 'determinante');

-- Posesivos
INSERT INTO concepto (clave, categoria) VALUES ('mi', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('mis', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('tu-pos', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('tus', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('su', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('sus', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('nuestro', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('nuestra', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('nuestros', 'posesivo');
INSERT INTO concepto (clave, categoria) VALUES ('nuestras', 'posesivo');

-- Pronombres adicionales
INSERT INTO concepto (clave, categoria) VALUES ('usted', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('ustedes-pron', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('el-pron', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('ella', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('ellos-pron', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('ellas', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('nosotros-pron', 'pronombre');

-- Interrogativos (variantes). "cuantas" no es un concepto aparte: es otra forma
-- de "cuantos" (kai / wak), y se agrega más abajo como palabra de ese concepto.
INSERT INTO concepto (clave, categoria) VALUES ('quienes', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('como', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('cual', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('cuales', 'interrogativo');

-- Sinónimos y conceptos compuestos
INSERT INTO concepto (clave, categoria) VALUES ('territorio-reclamado', 'sustantivo');

-- Preposiciones, conjunciones y negación
INSERT INTO concepto (clave, categoria) VALUES ('a', 'preposicion');
INSERT INTO concepto (clave, categoria) VALUES ('de', 'preposicion');
INSERT INTO concepto (clave, categoria) VALUES ('en', 'preposicion');
INSERT INTO concepto (clave, categoria) VALUES ('con', 'preposicion');
INSERT INTO concepto (clave, categoria) VALUES ('para', 'preposicion');
INSERT INTO concepto (clave, categoria) VALUES ('por', 'preposicion');
INSERT INTO concepto (clave, categoria) VALUES ('y', 'conjuncion');
INSERT INTO concepto (clave, categoria) VALUES ('o', 'conjuncion');
INSERT INTO concepto (clave, categoria) VALUES ('pero', 'conjuncion');
INSERT INTO concepto (clave, categoria) VALUES ('no', 'negacion');

-- ============================================================
-- PALABRAS ESPAÑOLAS (formas que apuntan a conceptos)
-- ============================================================

-- Artículos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'el-art'), 'es', 'el', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'la'), 'es', 'la', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'los'), 'es', 'los', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'las'), 'es', 'las', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'un'), 'es', 'un', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'una'), 'es', 'una', 7, '2026-11-23');

-- Posesivos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'mi'), 'es', 'mi', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'mis'), 'es', 'mis', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'tu-pos'), 'es', 'tu', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'tus'), 'es', 'tus', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'su'), 'es', 'su', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'sus'), 'es', 'sus', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'nuestro'), 'es', 'nuestro', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'nuestra'), 'es', 'nuestra', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'nuestros'), 'es', 'nuestros', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'nuestras'), 'es', 'nuestras', 7, '2026-11-23');

-- Pronombres
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'usted'), 'es', 'usted', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ustedes-pron'), 'es', 'ustedes', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'el-pron'), 'es', 'el', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ella'), 'es', 'ella', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ellos-pron'), 'es', 'ellos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ellas'), 'es', 'ellas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'nosotros-pron'), 'es', 'nosotros', 7, '2026-11-23');

-- Interrogativos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'cuantos'), 'es', 'cuantas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'quienes'), 'es', 'quienes', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'como'), 'es', 'como', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'cual'), 'es', 'cual', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'cuales'), 'es', 'cuales', 7, '2026-11-23');

-- Preposiciones, conjunciones y negación
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'a'), 'es', 'a', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'de'), 'es', 'de', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'en'), 'es', 'en', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'con'), 'es', 'con', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'para'), 'es', 'para', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'por'), 'es', 'por', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'y'), 'es', 'y', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'o'), 'es', 'o', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'pero'), 'es', 'pero', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'no'), 'es', 'no', 7, '2026-11-23');

-- Sinónimos: "llegar" (→ venir) ya viene en manual_v07.sql; no se repite aquí.

-- Conceptos compuestos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'territorio-reclamado'), 'es', 'territorio reclamado', 7, '2026-11-23');

-- ============================================================
-- CONJUGACIONES VERBALES (formas comunes)
-- ============================================================

-- Verbo "venir" (llegar)
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vengo', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vienes', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'viene', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'venimos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vienen', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vine', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'viniste', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vino', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vinimos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vinieron', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vendre', 7, '2026-11-23'); 
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vendras', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vendran', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'vendremos', 7, '2026-11-23');

-- Verbo "enviar"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'envio', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'envias', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'envia', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'enviamos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'envian', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'envie', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'enviaste', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'enviaron', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'enviare', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'enviaran', 7, '2026-11-23');

-- Verbo "ver"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'veo', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'ves', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 've', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'vemos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'ven', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'vi', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'viste', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'vio', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'vimos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'vieron', 7, '2026-11-23');

-- Verbo "querer"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quiero', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quieres', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quiere', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'queremos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quieren', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quise', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quiso', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quisimos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'quisieron', 7, '2026-11-23');

-- Verbo "esperar"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'espero', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'espera', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperamos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperan', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'espere', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperaron', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperare', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperaran', 7, '2026-11-23');

-- Verbo "visitar"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visito', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visitas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visita', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visitamos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visitan', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visite', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visitaron', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visitaremos', 7, '2026-11-23');

-- Verbo "hablar"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hablo', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hablas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'habla', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hablamos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hablan', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hable', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hablaron', 7, '2026-11-23');

-- Verbo "escuchar"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escucho', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escuchas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escucha', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escuchamos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escuchan', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escucharon', 7, '2026-11-23');

-- Verbo "ser/estar"
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'soy', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'eres', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'es', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'somos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'son', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'fui', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'fue', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) 
VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'fueron', 7, '2026-11-23');

-- ============================================================
-- PLURALES Y CONJUGACIONES FALTANTES (Corrección de revisión)
-- ============================================================

-- Plurales de sustantivos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nave'), 'es', 'naves', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'planeta'), 'es', 'planetas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'estrella'), 'es', 'estrellas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tierra'), 'es', 'tierras', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'agua'), 'es', 'aguas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'mensaje'), 'es', 'mensajes', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ciclo'), 'es', 'ciclos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ano'), 'es', 'anos', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'luz'), 'es', 'luces', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'criatura'), 'es', 'criaturas', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hogar'), 'es', 'hogares', 7, '2026-11-23');

-- Conjugaciones de "venir" (llegar) que faltaban
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'llegan', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'llegaron', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'llega', 7, '2026-11-23');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'llego', 7, '2026-11-23');

-- ============================================================
-- PRONOMBRES DE OBJETO ANTES DEL VERBO (D-017)
-- "la", "los" y "las" ya están como determinantes: el parser los lee como
-- pronombre de objeto cuando van justo antes del verbo (memoria 7.4).
-- "le" y "les" se registran para que el corrector no los cambie por otra
-- palabra, pero no tienen respaldo en el manual: el parser los rechaza
-- como palabra desconocida (D-014, D-017).
-- ============================================================
INSERT INTO concepto (clave, categoria) VALUES ('me', 'clitico');
INSERT INTO concepto (clave, categoria) VALUES ('te', 'clitico');
INSERT INTO concepto (clave, categoria) VALUES ('nos', 'clitico');
INSERT INTO concepto (clave, categoria) VALUES ('lo', 'clitico');
INSERT INTO concepto (clave, categoria) VALUES ('le', 'clitico');
INSERT INTO concepto (clave, categoria) VALUES ('les', 'clitico');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'me'), 'es', 'me', 7, '2026-11-24');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'te'), 'es', 'te', 7, '2026-11-24');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nos'), 'es', 'nos', 7, '2026-11-24');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'lo'), 'es', 'lo', 7, '2026-11-24');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'le'), 'es', 'le', 7, '2026-11-24');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'les'), 'es', 'les', 7, '2026-11-24');
