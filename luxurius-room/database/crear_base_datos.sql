CREATE DATABASE IF NOT EXISTS luxurius_room
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE luxurius_room;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    rol VARCHAR(255) NOT NULL,
    activo BIT(1) NOT NULL DEFAULT b'1',
    creado_en DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('HUESPED', 'ANFITRION', 'ADMINISTRADOR'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alojamientos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    anfitrion_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    ciudad VARCHAR(255) NOT NULL,
    pais VARCHAR(255) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    reglas TEXT,
    politica_cancelacion VARCHAR(255) NOT NULL,
    estado VARCHAR(255) NOT NULL,
    calificacion_promedio DECIMAL(2,1),
    creado_en DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_alojamientos_ciudad_estado (ciudad, estado),
    CONSTRAINT fk_alojamientos_anfitrion FOREIGN KEY (anfitrion_id) REFERENCES usuarios (id),
    CONSTRAINT chk_alojamientos_tipo CHECK (tipo IN ('HOTEL', 'APARTAMENTO', 'CASA', 'HOSTAL', 'CABANA')),
    CONSTRAINT chk_alojamientos_politica CHECK (politica_cancelacion IN ('FLEXIBLE', 'MODERADA', 'ESTRICTA')),
    CONSTRAINT chk_alojamientos_estado CHECK (estado IN ('PENDIENTE_APROBACION', 'ACTIVO', 'SUSPENDIDO')),
    CONSTRAINT chk_alojamientos_calificacion CHECK (calificacion_promedio IS NULL OR calificacion_promedio BETWEEN 0 AND 5)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS habitaciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alojamiento_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    capacidad_maxima INT NOT NULL,
    camas VARCHAR(255),
    unidades_totales INT NOT NULL,
    precio_base_noche DECIMAL(10,2) NOT NULL,
    cargo_limpieza DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activa BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT fk_habitaciones_alojamiento FOREIGN KEY (alojamiento_id) REFERENCES alojamientos (id),
    CONSTRAINT chk_habitaciones_capacidad CHECK (capacidad_maxima > 0),
    CONSTRAINT chk_habitaciones_unidades CHECK (unidades_totales > 0),
    CONSTRAINT chk_habitaciones_precio CHECK (precio_base_noche >= 0),
    CONSTRAINT chk_habitaciones_limpieza CHECK (cargo_limpieza >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS imagenes_alojamiento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alojamiento_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    orden INT NOT NULL,
    es_principal BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    CONSTRAINT fk_imagenes_alojamiento FOREIGN KEY (alojamiento_id) REFERENCES alojamientos (id) ON DELETE CASCADE,
    CONSTRAINT chk_imagenes_orden CHECK (orden >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servicios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_servicios_codigo UNIQUE (codigo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alojamiento_servicio (
    alojamiento_id BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    PRIMARY KEY (alojamiento_id, servicio_id),
    CONSTRAINT fk_alojamiento_servicio_alojamiento FOREIGN KEY (alojamiento_id) REFERENCES alojamientos (id) ON DELETE CASCADE,
    CONSTRAINT fk_alojamiento_servicio_servicio FOREIGN KEY (servicio_id) REFERENCES servicios (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tarifas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    habitacion_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    precio_noche DECIMAL(10,2) NOT NULL,
    descripcion VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_tarifas_habitacion_fechas (habitacion_id, fecha_inicio, fecha_fin),
    CONSTRAINT fk_tarifas_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitaciones (id) ON DELETE CASCADE,
    CONSTRAINT chk_tarifas_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_tarifas_precio CHECK (precio_noche >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bloqueos_disponibilidad (
    id BIGINT NOT NULL AUTO_INCREMENT,
    habitacion_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    unidades_bloqueadas INT NOT NULL,
    motivo VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_bloqueos_habitacion_fechas (habitacion_id, fecha_inicio, fecha_fin),
    CONSTRAINT fk_bloqueos_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitaciones (id) ON DELETE CASCADE,
    CONSTRAINT chk_bloqueos_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_bloqueos_unidades CHECK (unidades_bloqueadas > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(255) NOT NULL,
    usuario_id BIGINT NOT NULL,
    habitacion_id BIGINT NOT NULL,
    fecha_entrada DATE NOT NULL,
    fecha_salida DATE NOT NULL,
    adultos INT NOT NULL,
    ninos INT NOT NULL,
    unidades INT NOT NULL,
    noches INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    cargos_adicionales DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado_reserva VARCHAR(255) NOT NULL,
    estado_pago VARCHAR(255) NOT NULL DEFAULT 'PENDIENTE_EN_SITIO',
    acepto_condiciones BIT(1) NOT NULL,
    cancelacion_tardia BIT(1) NOT NULL DEFAULT b'0',
    motivo_cancelacion VARCHAR(255),
    creado_en DATETIME(6) NOT NULL,
    actualizado_en DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reservas_codigo UNIQUE (codigo),
    KEY idx_reservas_habitacion_fechas (habitacion_id, fecha_entrada, fecha_salida),
    KEY idx_reservas_usuario (usuario_id),
    CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitaciones (id),
    CONSTRAINT chk_reservas_fechas CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT chk_reservas_huespedes CHECK (adultos >= 1 AND ninos >= 0 AND adultos + ninos <= 16),
    CONSTRAINT chk_reservas_unidades_noches CHECK (unidades > 0 AND noches > 0),
    CONSTRAINT chk_reservas_importes CHECK (subtotal >= 0 AND cargos_adicionales >= 0 AND total >= 0),
    CONSTRAINT chk_reservas_estado CHECK (estado_reserva IN ('PENDIENTE_CONFIRMACION', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA', 'NO_PRESENTADO')),
    CONSTRAINT chk_reservas_pago CHECK (estado_pago IN ('PENDIENTE_EN_SITIO', 'PAGADO_EN_SITIO'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reserva_noches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    precio_aplicado DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_reserva_noches_reserva_fecha (reserva_id, fecha),
    CONSTRAINT fk_reserva_noches_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id) ON DELETE CASCADE,
    CONSTRAINT chk_reserva_noches_precio CHECK (precio_aplicado >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS resenas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    alojamiento_id BIGINT NOT NULL,
    calificacion INT NOT NULL,
    comentario TEXT,
    estado VARCHAR(255) NOT NULL DEFAULT 'VISIBLE',
    reportada BIT(1) NOT NULL DEFAULT b'0',
    motivo_reporte VARCHAR(255),
    creado_en DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_resena_reserva UNIQUE (reserva_id),
    CONSTRAINT fk_resenas_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id),
    CONSTRAINT fk_resenas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_resenas_alojamiento FOREIGN KEY (alojamiento_id) REFERENCES alojamientos (id),
    CONSTRAINT chk_resenas_calificacion CHECK (calificacion BETWEEN 1 AND 5),
    CONSTRAINT chk_resenas_estado CHECK (estado IN ('VISIBLE', 'OCULTA'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT,
    accion VARCHAR(255) NOT NULL,
    entidad VARCHAR(255) NOT NULL,
    entidad_id BIGINT,
    detalle TEXT,
    creado_en DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_auditoria_usuario_fecha (usuario_id, creado_en),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE SET NULL
) ENGINE=InnoDB;