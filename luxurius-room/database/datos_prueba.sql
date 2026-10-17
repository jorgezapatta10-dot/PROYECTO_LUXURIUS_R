-- SOLO PARA DESARROLLO. No ejecutar en producción.
USE luxurius_room;

INSERT IGNORE INTO usuarios
    (id, nombre, apellido, email, password_hash, telefono, rol, activo, creado_en)
VALUES
    (1, 'Huesped', 'Demo', 'huesped@luxuriusroom.test', '$2a$10$y64ffpwHeRBuKjTfIFeAve6poMtNeZcPuUjmOg4fGX1lRi1Z4SdTu', '3000000001', 'HUESPED', b'1', '2026-01-01 12:00:00'),
    (2, 'Anfitrion', 'Demo', 'anfitrion@luxuriusroom.test', '$2a$10$y64ffpwHeRBuKjTfIFeAve6poMtNeZcPuUjmOg4fGX1lRi1Z4SdTu', '3000000002', 'ANFITRION', b'1', '2026-01-01 12:00:00'),
    (3, 'Administrador', 'Demo', 'admin@luxuriusroom.test', '$2a$10$y64ffpwHeRBuKjTfIFeAve6poMtNeZcPuUjmOg4fGX1lRi1Z4SdTu', '3000000003', 'ADMINISTRADOR', b'1', '2026-01-01 12:00:00');

INSERT IGNORE INTO servicios (id, codigo, nombre)
VALUES
    (1, 'WIFI', 'Wi-Fi'),
    (2, 'ESTACIONAMIENTO', 'Estacionamiento'),
    (3, 'DESAYUNO', 'Desayuno'),
    (4, 'AIRE_ACONDICIONADO', 'Aire acondicionado'),
    (5, 'PISCINA', 'Piscina'),
    (6, 'COCINA', 'Cocina');

INSERT IGNORE INTO alojamientos
    (id, anfitrion_id, nombre, tipo, descripcion, ciudad, pais, direccion, reglas, politica_cancelacion, estado, calificacion_promedio, creado_en)
VALUES
    (1, 2, 'Casa Verde Laureles', 'CASA', 'Casa luminosa cerca de parques y restaurantes.', 'Medellin', 'Colombia', 'Carrera 74 # 38-20', 'No fumar en interiores.', 'FLEXIBLE', 'ACTIVO', 5.0, '2026-01-02 10:00:00'),
    (2, 2, 'Apartamento Centro Historico', 'APARTAMENTO', 'Apartamento equipado en una zona historica.', 'Cartagena', 'Colombia', 'Calle del Jardin # 5-18', 'Respetar el descanso de los vecinos.', 'MODERADA', 'ACTIVO', NULL, '2026-01-03 10:00:00'),
    (3, 2, 'Hotel Chapinero', 'HOTEL', 'Habitaciones privadas con acceso a transporte.', 'Bogota', 'Colombia', 'Carrera 13 # 57-40', 'Registro de todos los huespedes.', 'ESTRICTA', 'ACTIVO', NULL, '2026-01-04 10:00:00'),
    (4, 2, 'Hostal San Antonio', 'HOSTAL', 'Alojamiento sencillo en un barrio tradicional.', 'Cali', 'Colombia', 'Calle 4 Oeste # 3-12', 'Silencio despues de las 22:00.', 'FLEXIBLE', 'ACTIVO', NULL, '2026-01-05 10:00:00'),
    (5, 2, 'Apartamento El Poblado', 'APARTAMENTO', 'Espacio moderno con cocina equipada.', 'Medellin', 'Colombia', 'Calle 10 # 30-45', 'No se permiten fiestas.', 'MODERADA', 'ACTIVO', NULL, '2026-01-06 10:00:00'),
    (6, 2, 'Casa frente al mar', 'CASA', 'Casa familiar a pocos pasos de la playa.', 'Cartagena', 'Colombia', 'Carrera 2 # 8-60', 'Cuidar el mobiliario y cerrar ventanas.', 'ESTRICTA', 'ACTIVO', NULL, '2026-01-07 10:00:00'),
    (7, 2, 'Hotel Parque Central', 'HOTEL', 'Hotel urbano cercano a museos y oficinas.', 'Bogota', 'Colombia', 'Calle 26 # 7-30', 'Identificacion requerida al ingresar.', 'FLEXIBLE', 'ACTIVO', NULL, '2026-01-08 10:00:00'),
    (8, 2, 'Hostal Jardin del Rio', 'HOSTAL', 'Hostal tranquilo con areas compartidas.', 'Cali', 'Colombia', 'Avenida 6N # 22-15', 'Mantener limpias las areas comunes.', 'MODERADA', 'ACTIVO', NULL, '2026-01-09 10:00:00'),
    (9, 2, 'Cabana Alto de Palmas', 'CABANA', 'Cabana de montana pendiente de revision.', 'Medellin', 'Colombia', 'Via Las Palmas km 12', 'No hacer fogatas fuera de las zonas permitidas.', 'FLEXIBLE', 'PENDIENTE_APROBACION', NULL, '2026-01-10 10:00:00'),
    (10, 2, 'Apartamento suspendido demo', 'APARTAMENTO', 'Registro para probar la moderacion administrativa.', 'Bogota', 'Colombia', 'Calle 80 # 15-20', 'Cumplir el reglamento del edificio.', 'MODERADA', 'SUSPENDIDO', NULL, '2026-01-11 10:00:00');

INSERT IGNORE INTO habitaciones
    (id, alojamiento_id, nombre, descripcion, capacidad_maxima, camas, unidades_totales, precio_base_noche, cargo_limpieza, activa)
VALUES
    (1, 1, 'Habitacion doble', 'Habitacion privada con bano.', 2, '1 doble', 2, 320.00, 50.00, b'1'),
    (2, 1, 'Habitacion familiar', 'Habitacion amplia para familias.', 4, '2 dobles', 1, 480.00, 70.00, b'1'),
    (3, 2, 'Estudio', 'Estudio con cocina pequena.', 2, '1 doble', 2, 280.00, 30.00, b'1'),
    (4, 2, 'Apartamento familiar', 'Apartamento con sala y cocina.', 4, '1 doble + 2 sencillas', 1, 520.00, 60.00, b'1'),
    (5, 3, 'Habitacion estandar', 'Habitacion con escritorio.', 2, '1 doble', 5, 300.00, 25.00, b'1'),
    (6, 3, 'Habitacion ejecutiva', 'Habitacion amplia y silenciosa.', 2, '1 queen', 2, 410.00, 25.00, b'1'),
    (7, 4, 'Cama en habitacion compartida', 'Cama individual en dormitorio compartido.', 1, '1 sencilla', 6, 90.00, 0.00, b'1'),
    (8, 4, 'Habitacion privada', 'Habitacion privada para dos personas.', 2, '1 doble', 2, 190.00, 15.00, b'1'),
    (9, 5, 'Loft', 'Loft con cocina y balcon.', 2, '1 queen', 2, 390.00, 45.00, b'1'),
    (10, 5, 'Suite', 'Suite con sala independiente.', 3, '1 queen + 1 sencilla', 1, 560.00, 55.00, b'1'),
    (11, 6, 'Habitacion vista mar', 'Habitacion con vista al mar.', 2, '1 doble', 3, 450.00, 60.00, b'1'),
    (12, 6, 'Habitacion familiar', 'Habitacion para grupos pequenos.', 4, '2 dobles', 2, 620.00, 75.00, b'1'),
    (13, 7, 'Habitacion clasica', 'Habitacion comoda para viajes de trabajo.', 2, '1 doble', 4, 330.00, 20.00, b'1'),
    (14, 7, 'Habitacion superior', 'Habitacion superior con zona de trabajo.', 2, '1 queen', 2, 470.00, 20.00, b'1'),
    (15, 8, 'Habitacion compartida', 'Cama individual con casillero.', 1, '1 sencilla', 8, 80.00, 0.00, b'1'),
    (16, 8, 'Habitacion privada', 'Habitacion privada con ventilador.', 2, '1 doble', 2, 170.00, 10.00, b'1'),
    (17, 9, 'Cabana principal', 'Cabana para una pareja.', 2, '1 doble', 1, 350.00, 40.00, b'1'),
    (18, 9, 'Cabana familiar', 'Cabana con espacio para una familia.', 4, '2 dobles', 1, 510.00, 50.00, b'1'),
    (19, 10, 'Estudio demo', 'Habitacion de un alojamiento suspendido.', 2, '1 doble', 1, 260.00, 20.00, b'1'),
    (20, 10, 'Habitacion demo', 'Habitacion adicional para pruebas.', 3, '1 doble + 1 sencilla', 1, 340.00, 20.00, b'1');

INSERT IGNORE INTO imagenes_alojamiento (id, alojamiento_id, url, orden, es_principal)
VALUES
    (1, 1, 'https://picsum.photos/seed/lr-casa-verde-1/1200/800', 0, b'1'), (2, 1, 'https://picsum.photos/seed/lr-casa-verde-2/1200/800', 1, b'0'), (3, 1, 'https://picsum.photos/seed/lr-casa-verde-3/1200/800', 2, b'0'),
    (4, 2, 'https://picsum.photos/seed/lr-centro-historico-1/1200/800', 0, b'1'), (5, 2, 'https://picsum.photos/seed/lr-centro-historico-2/1200/800', 1, b'0'), (6, 2, 'https://picsum.photos/seed/lr-centro-historico-3/1200/800', 2, b'0'),
    (7, 3, 'https://picsum.photos/seed/lr-hotel-chapinero-1/1200/800', 0, b'1'), (8, 3, 'https://picsum.photos/seed/lr-hotel-chapinero-2/1200/800', 1, b'0'), (9, 3, 'https://picsum.photos/seed/lr-hotel-chapinero-3/1200/800', 2, b'0'),
    (10, 4, 'https://picsum.photos/seed/lr-hostal-san-antonio-1/1200/800', 0, b'1'), (11, 4, 'https://picsum.photos/seed/lr-hostal-san-antonio-2/1200/800', 1, b'0'), (12, 4, 'https://picsum.photos/seed/lr-hostal-san-antonio-3/1200/800', 2, b'0'),
    (13, 5, 'https://picsum.photos/seed/lr-el-poblado-1/1200/800', 0, b'1'), (14, 5, 'https://picsum.photos/seed/lr-el-poblado-2/1200/800', 1, b'0'), (15, 5, 'https://picsum.photos/seed/lr-el-poblado-3/1200/800', 2, b'0'),
    (16, 6, 'https://picsum.photos/seed/lr-casa-mar-1/1200/800', 0, b'1'), (17, 6, 'https://picsum.photos/seed/lr-casa-mar-2/1200/800', 1, b'0'), (18, 6, 'https://picsum.photos/seed/lr-casa-mar-3/1200/800', 2, b'0'),
    (19, 7, 'https://picsum.photos/seed/lr-parque-central-1/1200/800', 0, b'1'), (20, 7, 'https://picsum.photos/seed/lr-parque-central-2/1200/800', 1, b'0'), (21, 7, 'https://picsum.photos/seed/lr-parque-central-3/1200/800', 2, b'0'),
    (22, 8, 'https://picsum.photos/seed/lr-jardin-rio-1/1200/800', 0, b'1'), (23, 8, 'https://picsum.photos/seed/lr-jardin-rio-2/1200/800', 1, b'0'), (24, 8, 'https://picsum.photos/seed/lr-jardin-rio-3/1200/800', 2, b'0'),
    (25, 9, 'https://picsum.photos/seed/lr-cabana-palmas-1/1200/800', 0, b'1'), (26, 9, 'https://picsum.photos/seed/lr-cabana-palmas-2/1200/800', 1, b'0'), (27, 9, 'https://picsum.photos/seed/lr-cabana-palmas-3/1200/800', 2, b'0'),
    (28, 10, 'https://picsum.photos/seed/lr-suspendido-1/1200/800', 0, b'1'), (29, 10, 'https://picsum.photos/seed/lr-suspendido-2/1200/800', 1, b'0'), (30, 10, 'https://picsum.photos/seed/lr-suspendido-3/1200/800', 2, b'0');

INSERT IGNORE INTO alojamiento_servicio (alojamiento_id, servicio_id)
VALUES
    (1, 1), (1, 2), (1, 6), (2, 1), (2, 3), (2, 6), (3, 1), (3, 2), (3, 3),
    (4, 1), (4, 3), (5, 1), (5, 4), (5, 6), (6, 1), (6, 4), (6, 5),
    (7, 1), (7, 2), (7, 3), (8, 1), (8, 3), (9, 1), (9, 5), (9, 6),
    (10, 1), (10, 2), (10, 6);

INSERT IGNORE INTO tarifas (id, habitacion_id, fecha_inicio, fecha_fin, precio_noche, descripcion)
VALUES
    (1, 1, '2026-12-01', '2026-12-31', 390.00, 'Temporada de fin de ano'),
    (2, 3, '2026-12-01', '2026-12-31', 340.00, 'Temporada de fin de ano'),
    (3, 11, '2026-12-15', '2027-01-15', 550.00, 'Temporada alta');

INSERT IGNORE INTO bloqueos_disponibilidad
    (id, habitacion_id, fecha_inicio, fecha_fin, unidades_bloqueadas, motivo)
VALUES
    (1, 2, '2026-11-10', '2026-11-12', 1, 'Mantenimiento programado');

INSERT IGNORE INTO reservas
    (id, codigo, usuario_id, habitacion_id, fecha_entrada, fecha_salida, adultos, ninos, unidades, noches, subtotal, cargos_adicionales, total, estado_reserva, estado_pago, acepto_condiciones, cancelacion_tardia, motivo_cancelacion, creado_en, actualizado_en)
VALUES
    (1, 'LR-DEMO0001', 1, 1, '2026-06-02', '2026-06-04', 2, 0, 1, 2, 640.00, 50.00, 690.00, 'COMPLETADA', 'PAGADO_EN_SITIO', b'1', b'0', NULL, '2026-05-01 12:00:00', '2026-06-04 12:00:00'),
    (2, 'LR-DEMO0002', 1, 3, '2026-07-10', '2026-07-12', 2, 0, 1, 2, 560.00, 30.00, 590.00, 'CANCELADA', 'PENDIENTE_EN_SITIO', b'1', b'0', 'Cambio de planes', '2026-06-01 12:00:00', '2026-06-05 12:00:00');

INSERT IGNORE INTO reserva_noches (id, reserva_id, fecha, precio_aplicado)
VALUES
    (1, 1, '2026-06-02', 320.00), (2, 1, '2026-06-03', 320.00),
    (3, 2, '2026-07-10', 280.00), (4, 2, '2026-07-11', 280.00);

INSERT IGNORE INTO resenas
    (id, reserva_id, usuario_id, alojamiento_id, calificacion, comentario, estado, reportada, motivo_reporte, creado_en)
VALUES
    (1, 1, 1, 1, 5, 'Alojamiento limpio y bien ubicado. La atencion fue excelente.', 'VISIBLE', b'0', NULL, '2026-06-05 10:00:00');