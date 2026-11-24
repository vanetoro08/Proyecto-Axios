-- Morfología verbal española (PP-16).
-- Terminaciones regulares y verbos irregulares.
-- Se carga DESPUÉS de manual_v07.sql y vocabulario_es.sql.

-- ============================================================
-- TERMINACIONES REGULARES
-- ============================================================

-- Presente (regulares -ar)
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('o', 'ar', 'presente', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('as', 'ar', 'presente', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('a', 'ar', 'presente', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('amos', 'ar', 'presente', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('an', 'ar', 'presente', 3, 'plural');

-- Presente (regulares -er)
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('o', 'er', 'presente', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('es', 'er', 'presente', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('e', 'er', 'presente', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('emos', 'er', 'presente', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('en', 'er', 'presente', 3, 'plural');

-- Presente (regulares -ir)
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('o', 'ir', 'presente', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('es', 'ir', 'presente', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('e', 'ir', 'presente', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('imos', 'ir', 'presente', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('en', 'ir', 'presente', 3, 'plural');

-- Pasado (regulares -ar)
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('e', 'ar', 'pasado', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('aste', 'ar', 'pasado', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('o', 'ar', 'pasado', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('amos', 'ar', 'pasado', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('aron', 'ar', 'pasado', 3, 'plural');

-- Pasado (regulares -er, -ir)
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('i', 'er', 'pasado', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('iste', 'er', 'pasado', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('io', 'er', 'pasado', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('imos', 'er', 'pasado', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) 
VALUES ('ieron', 'er', 'pasado', 3, 'plural');

-- Futuro 
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('are', 'ar', 'futuro', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('aras', 'ar', 'futuro', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('ara', 'ar', 'futuro', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('aremos', 'ar', 'futuro', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('aran', 'ar', 'futuro', 3, 'plural');

INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('ere', 'er', 'futuro', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('eras', 'er', 'futuro', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('era', 'er', 'futuro', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('eremos', 'er', 'futuro', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('eran', 'er', 'futuro', 3, 'plural');

INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('ire', 'ir', 'futuro', 1, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('iras', 'ir', 'futuro', 2, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('ira', 'ir', 'futuro', 3, 'singular');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('iremos', 'ir', 'futuro', 1, 'plural');
INSERT INTO morfologia_es (terminacion, conjugacion, tiempo, persona, numero) VALUES ('iran', 'ir', 'futuro', 3, 'plural');

-- ============================================================
-- VERBOS IRREGULARES (del vocabulario y de las señales)
-- ============================================================

-- Venir / Llegar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vengo', 'venir', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vienes', 'venir', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('viene', 'venir', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('venimos', 'venir', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vienen', 'venir', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vine', 'venir', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('viniste', 'venir', 'pasado', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vino', 'venir', 'pasado', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vinimos', 'venir', 'pasado', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vinieron', 'venir', 'pasado', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vendre', 'venir', 'futuro', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vendras', 'venir', 'futuro', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vendran', 'venir', 'futuro', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vendremos', 'venir', 'futuro', 1, 'plural');

-- Enviar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('envio', 'enviar', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('envias', 'enviar', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('envia', 'enviar', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('enviamos', 'enviar', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('envian', 'enviar', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('envie', 'enviar', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('enviaste', 'enviar', 'pasado', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('enviaron', 'enviar', 'pasado', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('enviare', 'enviar', 'futuro', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('enviaran', 'enviar', 'futuro', 3, 'plural');

-- Ver
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('veo', 'ver', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('ves', 'ver', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('ve', 'ver', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vemos', 'ver', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('ven', 'ver', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vi', 'ver', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('viste', 'ver', 'pasado', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vio', 'ver', 'pasado', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vimos', 'ver', 'pasado', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vieron', 'ver', 'pasado', 3, 'plural');

-- Querer
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quiero', 'querer', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quieres', 'querer', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quiere', 'querer', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('queremos', 'querer', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quieren', 'querer', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quise', 'querer', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quiso', 'querer', 'pasado', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quisimos', 'querer', 'pasado', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('quisieron', 'querer', 'pasado', 3, 'plural');

-- Esperar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('espero', 'esperar', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('esperas', 'esperar', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('espera', 'esperar', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('esperamos', 'esperar', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('esperan', 'esperar', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('espere', 'esperar', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('esperaron', 'esperar', 'pasado', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('esperare', 'esperar', 'futuro', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('esperaran', 'esperar', 'futuro', 3, 'plural');

-- Visitar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visito', 'visitar', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visitas', 'visitar', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visita', 'visitar', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visitamos', 'visitar', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visitan', 'visitar', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visite', 'visitar', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visitaron', 'visitar', 'pasado', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('visitaremos', 'visitar', 'futuro', 1, 'plural');

-- Hablar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('hablo', 'hablar', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('hablas', 'hablar', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('habla', 'hablar', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('hablamos', 'hablar', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('hablan', 'hablar', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('hable', 'hablar', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('hablaron', 'hablar', 'pasado', 3, 'plural');

-- Escuchar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('escucho', 'escuchar', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('escuchas', 'escuchar', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('escucha', 'escuchar', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('escuchamos', 'escuchar', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('escuchan', 'escuchar', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('escucharon', 'escuchar', 'pasado', 3, 'plural');

-- Ser/Estar
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('soy', 'ser', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('eres', 'ser', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('es', 'ser', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('somos', 'ser', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('son', 'ser', 'presente', 3, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('fui', 'ser', 'pasado', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('fue', 'ser', 'pasado', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('fueron', 'ser', 'pasado', 3, 'plural');

-- Ir (para el futuro perífrástico "ir a + inf")
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('voy', 'ir', 'presente', 1, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vas', 'ir', 'presente', 2, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('va', 'ir', 'presente', 3, 'singular');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('vamos', 'ir', 'presente', 1, 'plural');
INSERT INTO verbo_irregular (forma, infinitivo, tiempo, persona, numero) VALUES ('van', 'ir', 'presente', 3, 'plural');