-- Datos del manual de decodificación v0.7 (enunciado, sección 3) y reglas de la bitácora.
-- PP-29. Solo se ejecuta si la BD está vacía. Español sin tildes ni ñ (D-003).
-- La corrección de teka (manual 0.8) NO va aquí: entra con `cargar data/senales_semana2.txt`.

-- Fuentes
INSERT INTO fuente (id, tipo, descripcion, fecha) VALUES (1, 'manual', 'Manual de decodificación v0.7', '2026-11-10');
INSERT INTO fuente (id, tipo, descripcion, fecha) VALUES (2, 'decision', 'Bitácora D-004: nosotros según el tiempo', '2026-11-21');
INSERT INTO fuente (id, tipo, descripcion, fecha) VALUES (3, 'decision', 'Bitácora D-007: números de Kethari en base 6', '2026-11-21');
INSERT INTO fuente (id, tipo, descripcion, fecha) VALUES (4, 'decision', 'Bitácora D-008: partícula de Ozh en preguntas', '2026-11-21');
INSERT INTO fuente (id, tipo, descripcion, fecha) VALUES (5, 'decision', 'Bitácora D-013: orden de poseedor y número en Kethari', '2026-11-21');
INSERT INTO fuente (id, tipo, descripcion, fecha) VALUES (6, 'decision', 'Bitácora D-016: partícula de Ozh en afirmaciones', '2026-11-22');

-- Conceptos (claves neutrales del árbol intermedio)
INSERT INTO concepto (clave, categoria) VALUES ('yo', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('tu', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('el', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('nosotros', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('ustedes', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('ellos', 'pronombre');
INSERT INTO concepto (clave, categoria) VALUES ('planeta', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('estrella', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('tierra', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('agua', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('nave', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('mensaje', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('paz', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('ciclo', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('ano', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('luz', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('criatura', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('hogar', 'sustantivo');
INSERT INTO concepto (clave, categoria) VALUES ('venir', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('enviar', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('ver', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('querer', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('ser', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('hablar', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('escuchar', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('esperar', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('visitar', 'verbo');
INSERT INTO concepto (clave, categoria) VALUES ('que', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('cuando', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('donde', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('cuantos', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('quien', 'interrogativo');
INSERT INTO concepto (clave, categoria) VALUES ('cero', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('uno', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('dos', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('tres', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('cuatro', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('cinco', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('seis', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('siete', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('ocho', 'digito');
INSERT INTO concepto (clave, categoria) VALUES ('nueve', 'digito');

-- Léxico (manual 3.1)
-- yo
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'yo'), 'es', 'yo', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'yo'), 'kethari', 'an', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'yo'), 'ozh', 'ʔen', 1, '2026-11-10');
-- tu
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tu'), 'es', 'tu', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tu'), 'es', 'usted', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tu'), 'kethari', 'ul', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tu'), 'ozh', 'sha', 1, '2026-11-10');
-- el
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'el'), 'es', 'el', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'el'), 'es', 'ella', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'el'), 'kethari', 'ir', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'el'), 'ozh', 'oru', 1, '2026-11-10');
-- nosotros
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nosotros'), 'es', 'nosotros', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nosotros'), 'kethari', 'anul', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nosotros'), 'kethari', 'anir', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nosotros'), 'ozh', 'ʔenka', 1, '2026-11-10');
-- ustedes
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ustedes'), 'es', 'ustedes', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ustedes'), 'kethari', 'ulesh', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ustedes'), 'ozh', 'shavi', 1, '2026-11-10');
-- ellos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ellos'), 'es', 'ellos', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ellos'), 'es', 'ellas', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ellos'), 'kethari', 'iresh', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ellos'), 'ozh', 'oruvi', 1, '2026-11-10');
-- planeta
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'planeta'), 'es', 'planeta', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'planeta'), 'es', 'mundo', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'planeta'), 'kethari', 'vael', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'planeta'), 'ozh', 'thüm', 1, '2026-11-10');
-- estrella
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'estrella'), 'es', 'estrella', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'estrella'), 'kethari', 'sorin', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'estrella'), 'ozh', 'ral', 1, '2026-11-10');
-- tierra
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tierra'), 'es', 'tierra', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tierra'), 'kethari', 'teka', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tierra'), 'ozh', 'mak', 1, '2026-11-10');
-- agua
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'agua'), 'es', 'agua', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'agua'), 'kethari', 'dahl', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'agua'), 'ozh', 'naʔ', 1, '2026-11-10');
-- nave
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nave'), 'es', 'nave', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nave'), 'kethari', 'kum', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nave'), 'ozh', 'gor', 1, '2026-11-10');
-- mensaje
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'mensaje'), 'es', 'mensaje', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'mensaje'), 'kethari', 'ombra', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'mensaje'), 'ozh', 'vesh', 1, '2026-11-10');
-- paz
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'paz'), 'es', 'paz', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'paz'), 'kethari', 'parn', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'paz'), 'ozh', 'ilu', 1, '2026-11-10');
-- ciclo
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ciclo'), 'es', 'ciclo', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ciclo'), 'kethari', 'lun', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ciclo'), 'ozh', 'drüm', 1, '2026-11-10');
-- ano
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ano'), 'es', 'ano', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ano'), 'kethari', 'sorlun', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ano'), 'ozh', 'drümak', 1, '2026-11-10');
-- luz
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'luz'), 'es', 'luz', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'luz'), 'kethari', 'zett', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'luz'), 'ozh', 'osk', 1, '2026-11-10');
-- criatura
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'criatura'), 'es', 'criatura', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'criatura'), 'kethari', 'yorr', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'criatura'), 'ozh', 'fen', 1, '2026-11-10');
-- hogar
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hogar'), 'es', 'hogar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hogar'), 'kethari', 'hosk', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hogar'), 'ozh', 'beth', 1, '2026-11-10');
-- venir
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'venir', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'es', 'llegar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'kethari', 'vin', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'venir'), 'ozh', 'sur', 1, '2026-11-10');
-- enviar
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'es', 'enviar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'kethari', 'sal', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'enviar'), 'ozh', 'dra', 1, '2026-11-10');
-- ver
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'es', 'ver', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'kethari', 'kor', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ver'), 'ozh', 'ʔim', 1, '2026-11-10');
-- querer
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'es', 'querer', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'kethari', 'thal', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'querer'), 'ozh', 'lom', 1, '2026-11-10');
-- ser
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'ser', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'es', 'estar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'kethari', 'mi', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ser'), 'ozh', 'ha', 1, '2026-11-10');
-- hablar
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'es', 'hablar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'kethari', 'lar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'hablar'), 'ozh', 'tal', 1, '2026-11-10');
-- escuchar
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'es', 'escuchar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'kethari', 'rhu', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'escuchar'), 'ozh', 'nem', 1, '2026-11-10');
-- esperar
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'es', 'esperar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'kethari', 'tep', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'esperar'), 'ozh', 'bo', 1, '2026-11-10');
-- visitar
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'es', 'visitar', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'kethari', 'dun', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'visitar'), 'ozh', 'kesh', 1, '2026-11-10');
-- que
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'que'), 'es', 'que', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'que'), 'kethari', 'kel', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'que'), 'ozh', 'wu', 1, '2026-11-10');
-- cuando
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuando'), 'es', 'cuando', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuando'), 'kethari', 'kom', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuando'), 'ozh', 'wem', 1, '2026-11-10');
-- donde
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'donde'), 'es', 'donde', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'donde'), 'kethari', 'dor', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'donde'), 'ozh', 'wor', 1, '2026-11-10');
-- cuantos
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuantos'), 'es', 'cuantos', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuantos'), 'kethari', 'kai', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuantos'), 'ozh', 'wak', 1, '2026-11-10');
-- quien
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'quien'), 'es', 'quien', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'quien'), 'kethari', 'sem', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'quien'), 'ozh', 'wis', 1, '2026-11-10');

-- Números (manual 3.2). Kethari solo tiene del 0 al 5.
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cero'), 'kethari', 'vo', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cero'), 'ozh', 'zu', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'uno'), 'kethari', 'ti', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'uno'), 'ozh', 'ois', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'dos'), 'kethari', 'ra', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'dos'), 'ozh', 'dav', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tres'), 'kethari', 'sel', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'tres'), 'ozh', 'kru', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuatro'), 'kethari', 'mok', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cuatro'), 'ozh', 'fem', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cinco'), 'kethari', 'pai', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'cinco'), 'ozh', 'ulo', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'seis'), 'ozh', 'sab', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'siete'), 'ozh', 'nir', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'ocho'), 'ozh', 'tok', 1, '2026-11-10');
INSERT INTO palabra (concepto_id, lengua, forma, fuente_id, vigente_desde) VALUES ((SELECT id FROM concepto WHERE clave = 'nueve'), 'ozh', 'hev', 1, '2026-11-10');

-- Reglas gramaticales (manual 3.3 y 3.4)
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'orden', 'SOV', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'sufijo.pasado', 'ok', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'sufijo.futuro', 'ime', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'prefijo.negacion', 'nu', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'sufijo.plural', 'esh', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'posicion.numero', 'antes', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'particula.pregunta', 'ka', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'posicion.interrogativa', 'inicio', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'posicion.poseedor', 'antes', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'numeros.separador', '-', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'orden', 'VSO', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'posicion.particula', 'despues.verbo', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'marcador.pasado', 'ʔoka', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'marcador.futuro', 'venu', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'posicion.marcador', 'inicio', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'negacion', 'ne', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'posicion.negacion', 'antes.verbo', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'plural', 'no', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'prefijo.pregunta', 'ʔa', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'posicion.interrogativa', 'final', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'posicion.poseedor', 'despues', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'numeros.base', '10', 1, '2026-11-10');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'numeros.separador', '-', 1, '2026-11-10');

-- Reglas que salen de la bitácora
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'nosotros.pasado', 'anir', 2, '2026-11-21');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'nosotros.presente', 'anul', 2, '2026-11-21');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'nosotros.futuro', 'anul', 2, '2026-11-21');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'numeros.base', '6', 3, '2026-11-21');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'particula.pregunta.defecto', 'mar', 4, '2026-11-21');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('kethari', 'orden.poseedor.numero', 'poseedor,numero,sustantivo', 5, '2026-11-21');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'particula.afirmacion.pasado', 'zi', 6, '2026-11-22');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'particula.afirmacion.terceros', 'tho', 6, '2026-11-22');
INSERT INTO regla (lengua, clave, valor, fuente_id, vigente_desde) VALUES ('ozh', 'particula.afirmacion.participantes', 'mar', 6, '2026-11-22');