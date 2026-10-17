# PROMPT PARA CREAR LUXURIUS ROOM (versión revisada)

> **Cómo usar este documento en Visual Studio Code**
> 1. Crea una carpeta vacía (por ejemplo `C:\Proyectos\luxurius-room`) y ábrela con *File → Open Folder*.
> 2. Abre el chat de tu asistente de IA en modo **agente** (Copilot Agent, Claude Code, Cline, etc.), de modo que pueda crear archivos y ejecutar comandos en la terminal.
> 3. Copia **todo lo que está debajo de la línea "INICIO DEL PROMPT"** y pégalo en el chat. (Opcional: guarda este archivo dentro de la carpeta como `PROMPT.md` y escribe en el chat: *"Lee PROMPT.md y ejecútalo completo, etapa por etapa"*).
> 4. Cuando la IA termine una etapa, escribe: **"Continúa con la siguiente etapa"**.

---

# INICIO DEL PROMPT

## 0. ROL Y OBJETIVO

Actúa como un desarrollador senior de Java y Spring Boot que además enseña programación a principiantes.

Crea desde cero una aplicación web completa llamada **Luxurius Room**: una plataforma de gestión y reserva de alojamientos inspirada en Booking, con diseño minimalista.

Quien te lo pide es principiante. Por eso:

- El código debe ser claro, ordenado, con comentarios breves **en español** donde ayuden a entender.
- Prefiere soluciones simples sobre soluciones "elegantes" pero complicadas.
- No agregues tecnologías, librerías ni patrones que no se pidan aquí.
- Nombres de tablas, entidades, endpoints, variables de negocio y textos de la interfaz: **en español** (sin tildes ni ñ en identificadores; por ejemplo `resenas`, `ninos`). Palabras técnicas estándar (`controller`, `service`, `token`) se quedan en inglés.

**Regla fundamental del proyecto:** NO existen pagos en línea. No implementes pasarela de pagos, formularios de tarjeta, ni guardes datos de tarjetas. El pago se realiza en el alojamiento.

---

## 1. TECNOLOGÍAS (cerradas)

| Capa | Tecnologías permitidas |
|---|---|
| Frontend | HTML5, CSS3, JavaScript puro (ES6+), Bootstrap 5.3 por CDN, Fetch API |
| Backend | Java 21, Spring Boot 3.3.x, Maven (con **Maven Wrapper**: `mvnw` y `mvnw.cmd`, para que no haya que instalar Maven), Spring Web, Spring Data JPA, Spring Security, Spring Boot Validation |
| Base de datos | MySQL 8.x, scripts SQL |
| Pruebas | JUnit 5 y Mockito (incluidos en `spring-boot-starter-test`). Se permite `H2` **solo con alcance `test`** si lo necesitas para una prueba de integración |

Autenticación por **token JWT firmado (HS256)** usando únicamente `spring-boot-starter-oauth2-resource-server` (trae Nimbus; no agregues `jjwt` ni otras librerías de JWT). Contraseñas con **BCrypt** (`BCryptPasswordEncoder`).

**Prohibido:** React, Angular, Vue, TypeScript, Node.js (ni como backend ni como herramienta de compilación), microservicios, Redux, Tailwind, Lombok, MapStruct, Thymeleaf, Docker (no es necesario en esta versión) y cualquier pasarela de pagos.

El frontend **no** requiere compilar nada: son archivos estáticos que se abren con un servidor estático simple.

---

## 2. ESTRUCTURA DEL PROYECTO

Crea exactamente esta estructura (puedes añadir los archivos marcados con `(+)`):

```
luxurius-room/
├── frontend/
│   ├── index.html
│   ├── alojamientos.html
│   ├── detalle-alojamiento.html
│   ├── login.html
│   ├── registro.html
│   ├── mis-reservas.html
│   ├── perfil.html
│   ├── panel-anfitrion.html
│   ├── panel-administrador.html
│   ├── css/
│   │   └── estilos.css
│   ├── js/
│   │   ├── app.js              (configuración API_URL, funciones comunes, navbar, manejo de token)
│   │   ├── autenticacion.js
│   │   ├── alojamientos.js
│   │   ├── reservas.js
│   │   └── panel.js            (panel anfitrión y panel administrador)
│   └── img/
├── backend/
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd, .mvn/   (+) Maven Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/luxuriusroom/
│       │   │   ├── LuxuriusRoomApplication.java
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   ├── model/      (entidades JPA)
│       │   │   ├── dto/        (+) objetos de entrada/salida de la API
│       │   │   ├── exception/  (+) excepciones propias y manejador global
│       │   │   └── config/     (seguridad, CORS, JWT)
│       │   └── resources/
│       │       └── application.properties
│       └── test/
├── database/
│   ├── crear_base_datos.sql
│   └── datos_prueba.sql
├── .gitignore                  (+)
└── README.md
```

Reglas:
- `frontend/` contiene solo HTML, CSS, JS e imágenes.
- `backend/` contiene solo la aplicación Java y su configuración.
- Las entidades JPA **no** se devuelven directamente en la API: usa DTOs sencillos (clases `record` de Java).
- Si ya existen archivos en la carpeta, léelos antes de crear nada y **no sobrescribas** trabajo existente sin revisarlo.

---

## 3. DISEÑO VISUAL

Elegante, moderno, minimalista, con mucho espacio en blanco, fotografías grandes y tipografía legible (usa la fuente del sistema o Inter de Google Fonts). Nada recargado.

Paleta (defínela como variables CSS en `:root` dentro de `estilos.css`):

| Uso | Color |
|---|---|
| Fondo | `#FFFFFF` |
| Fondo secundario | `#F7F7F7` |
| Texto | `#222222` |
| Texto secundario | `#717171` |
| Botón principal / enlaces | `#2563EB` |
| Confirmaciones / éxito | `#16803D` |

Componentes con Bootstrap 5: barra de navegación (cambia según el usuario esté o no autenticado y según su rol), formulario de búsqueda, tarjetas de alojamientos, botones, modales, tablas, formularios, paneles, alertas/toasts de confirmación y diseño adaptable a móvil, tableta y computador.

`estilos.css` solo personaliza Bootstrap (colores, bordes redondeados, sombras suaves, tarjetas con imagen). Bootstrap se carga por CDN (CSS y JS bundle) con versión fija en cada página.

---

## 4. REGLAS DE NEGOCIO (léelas antes de programar)

Estas reglas resuelven los puntos ambiguos del proyecto. Respétalas tal cual.

**Fechas y noches**
- `fecha_entrada` es el día de llegada y `fecha_salida` el día de salida. La noche de la salida **no** se cobra.
- Noches = `fecha_salida − fecha_entrada`. Mínimo 1 noche. La entrada no puede estar en el pasado (hoy sí es válido).
- Dos estancias se solapan si `entrada_A < salida_B` **y** `salida_A > entrada_B`. (Salir el día 10 y entrar el día 10 **no** es solapamiento.)

**Huéspedes**
- Máximo **16 huéspedes** por reserva (adultos + niños). Mínimo 1 adulto.
- `habitaciones` solicitadas ≥ 1 y no puede ser mayor que el número de adultos.
- Una reserva es sobre **un tipo de habitación** (`habitacion_id`) y una cantidad de unidades de ese tipo (`unidades`). La capacidad exigida es: `capacidad_maxima × unidades ≥ adultos + niños`.

**Habitaciones con varias unidades**
- Cada fila de `habitaciones` es un *tipo* de habitación con `unidades_totales` (por ejemplo, 5 habitaciones dobles iguales).
- Unidades disponibles de una habitación en una noche = `unidades_totales − unidades reservadas ese día (reservas en estado PENDIENTE_CONFIRMACION o CONFIRMADA) − unidades bloqueadas ese día`.
- La habitación está disponible para una estancia solo si, en **todas** las noches, las unidades disponibles son ≥ las solicitadas.

**Precio (siempre calculado en el servidor)**
- Precio de una noche = tarifa vigente de la habitación para esa fecha (tabla `tarifas`); si no hay tarifa para esa fecha, se usa `precio_base_noche` de la habitación.
- No puede haber dos tarifas de la misma habitación con rangos de fechas solapados (valídalo al crearlas).
- `subtotal = suma de los precios de cada noche × unidades`.
- `cargos_adicionales = cargo_limpieza de la habitación × unidades` (puede ser 0).
- `total = subtotal + cargos_adicionales`. Usa `BigDecimal` y 2 decimales. Nunca `double`.
- El precio de cada noche se guarda en `reserva_noches` para que cambios futuros de tarifa no alteren reservas existentes.
- El frontend puede mostrar un precio estimado, pero el servidor lo **recalcula** e ignora cualquier precio enviado por el cliente.

**Estados**
- Estado de la reserva: `PENDIENTE_CONFIRMACION` (inicial), `CONFIRMADA`, `CANCELADA`, `COMPLETADA`, `NO_PRESENTADO`.
- Estado del pago (independiente): `PENDIENTE_EN_SITIO` (inicial) y `PAGADO_EN_SITIO`. Solo el anfitrión dueño del alojamiento puede pasar a `PAGADO_EN_SITIO`, y solo si la reserva está `CONFIRMADA` o `COMPLETADA`. Nunca se marca como pagada automáticamente.
- Estado del alojamiento: `PENDIENTE_APROBACION` (inicial al crearlo), `ACTIVO`, `SUSPENDIDO`. Solo los `ACTIVO` aparecen en búsquedas y se pueden reservar. Solo el administrador aprueba o suspende.
- Transiciones permitidas de la reserva:
  - `PENDIENTE_CONFIRMACION → CONFIRMADA` (anfitrión) o `→ CANCELADA` (huésped o anfitrión).
  - `CONFIRMADA → CANCELADA` (huésped o anfitrión), `→ COMPLETADA` o `→ NO_PRESENTADO` (anfitrión, solo desde el día de entrada en adelante).
  - Cualquier otra transición se rechaza con error 409.

**Cancelación**
- El huésped solo cancela sus propias reservas y solo antes de la fecha de entrada.
- El alojamiento tiene `politica_cancelacion`: `FLEXIBLE` (cancelación gratuita hasta 24 horas antes de la entrada), `MODERADA` (hasta 5 días antes) o `ESTRICTA` (hasta 14 días antes). Cancelar fuera de plazo está permitido, pero la reserva queda marcada con `cancelacion_tardia = true` para que el anfitrión lo vea (no hay reembolsos porque no hay pagos en línea).
- Al cancelar se liberan las unidades automáticamente (porque las reservas canceladas no cuentan en la disponibilidad).

**Concurrencia (evitar doble reserva)**
- Al crear una reserva: dentro de una transacción `@Transactional`, bloquea la fila de la habitación con bloqueo pesimista (`@Lock(LockModeType.PESSIMISTIC_WRITE)` en el repositorio), vuelve a validar fechas, capacidad y disponibilidad, y solo entonces guarda. Así dos usuarios simultáneos no pueden reservar las mismas unidades.

**Código de reserva**
- Formato `LR-` + 8 caracteres alfanuméricos en mayúscula generados con `SecureRandom`, único (restricción `UNIQUE` en la base de datos; reintenta si hay colisión).

**Reseñas**
- Solo el huésped dueño de una reserva en estado `COMPLETADA` puede reseñarla, una sola vez por reserva (`UNIQUE(reserva_id)`). Calificación entera de 1 a 5 y comentario de hasta 1000 caracteres.
- La calificación promedio del alojamiento se recalcula al crear, ocultar o restaurar una reseña.
- Una reseña puede ser reportada (por el anfitrión del alojamiento) y el administrador decide ocultarla o restaurarla.

**Roles**
- `HUESPED`, `ANFITRION`, `ADMINISTRADOR`.
- En el registro público solo se puede elegir `HUESPED` o `ANFITRION`. **Nunca** se puede crear un administrador desde la API pública.
- Un anfitrión solo ve y modifica **sus** alojamientos y las reservas de **sus** alojamientos (verifica la propiedad en el servicio, no solo en el frontend).

---

## 5. BASE DE DATOS MySQL

Base de datos: `luxurius_room` (utf8mb4, collation `utf8mb4_unicode_ci`, motor InnoDB).

`crear_base_datos.sql` debe: crear la base si no existe, crear las tablas en orden correcto (respetando claves foráneas) y poder ejecutarse de nuevo sin errores (`CREATE TABLE IF NOT EXISTS`; opcionalmente `DROP DATABASE IF EXISTS` comentado y explicado).

Tablas y columnas mínimas (todas con `id BIGINT AUTO_INCREMENT PRIMARY KEY` salvo las de unión):

| Tabla | Columnas principales |
|---|---|
| `usuarios` | nombre, apellido, email (UNIQUE), password_hash, telefono, rol ENUM('HUESPED','ANFITRION','ADMINISTRADOR'), activo BOOLEAN, creado_en DATETIME |
| `alojamientos` | anfitrion_id FK→usuarios, nombre, tipo ENUM('HOTEL','APARTAMENTO','CASA','HOSTAL','CABANA'), descripcion TEXT, ciudad, pais, direccion, reglas TEXT, politica_cancelacion ENUM('FLEXIBLE','MODERADA','ESTRICTA'), estado ENUM('PENDIENTE_APROBACION','ACTIVO','SUSPENDIDO'), calificacion_promedio DECIMAL(2,1) NULL, creado_en |
| `habitaciones` | alojamiento_id FK, nombre, descripcion, capacidad_maxima INT, camas VARCHAR (ej. "1 doble + 1 sencilla"), unidades_totales INT, precio_base_noche DECIMAL(10,2), cargo_limpieza DECIMAL(10,2) DEFAULT 0, activa BOOLEAN |
| `imagenes_alojamiento` | alojamiento_id FK, url VARCHAR(500), orden INT, es_principal BOOLEAN |
| `servicios` | codigo (UNIQUE: WIFI, ESTACIONAMIENTO, DESAYUNO, AIRE_ACONDICIONADO, PISCINA, MASCOTAS, COCINA…), nombre |
| `alojamiento_servicio` | alojamiento_id FK, servicio_id FK, **PK compuesta** (alojamiento_id, servicio_id) |
| `tarifas` | habitacion_id FK, fecha_inicio DATE, fecha_fin DATE (inclusive), precio_noche DECIMAL(10,2), descripcion |
| `bloqueos_disponibilidad` | habitacion_id FK, fecha_inicio DATE, fecha_fin DATE (inclusive), unidades_bloqueadas INT, motivo |
| `reservas` | codigo (UNIQUE), usuario_id FK, habitacion_id FK, fecha_entrada DATE, fecha_salida DATE, adultos, ninos, unidades INT, noches INT, subtotal DECIMAL(10,2), cargos_adicionales DECIMAL(10,2), total DECIMAL(10,2), estado_reserva ENUM(...), estado_pago ENUM('PENDIENTE_EN_SITIO','PAGADO_EN_SITIO') DEFAULT 'PENDIENTE_EN_SITIO', acepto_condiciones BOOLEAN, cancelacion_tardia BOOLEAN DEFAULT FALSE, motivo_cancelacion, creado_en DATETIME, actualizado_en DATETIME |
| `reserva_noches` | reserva_id FK, fecha DATE, precio_aplicado DECIMAL(10,2) |
| `resenas` | reserva_id FK **UNIQUE**, usuario_id FK, alojamiento_id FK, calificacion TINYINT con `CHECK (calificacion BETWEEN 1 AND 5)`, comentario, estado ENUM('VISIBLE','OCULTA') DEFAULT 'VISIBLE', reportada BOOLEAN, motivo_reporte, creado_en |
| `auditoria` | usuario_id FK NULL, accion (ej. CREAR_RESERVA, CANCELAR_RESERVA, APROBAR_ALOJAMIENTO), entidad, entidad_id, detalle, creado_en |

Requisitos:
- Claves primarias, claves foráneas, índices en `alojamientos(ciudad, estado)`, `reservas(habitacion_id, fecha_entrada, fecha_salida)`, `reservas(usuario_id)`, y restricciones `CHECK` en fechas (`fecha_salida > fecha_entrada`), capacidades (> 0) y precios (≥ 0).
- Tipos: `DECIMAL` para dinero, `DATE` para fechas de estancia, `DATETIME` para registros de fecha/hora.
- No existe ninguna columna ni tabla para tarjetas o datos de pago.
- JPA se configura con `spring.jpa.hibernate.ddl-auto=validate`: **las tablas las crean los scripts SQL, no Hibernate**. Las entidades deben coincidir con las tablas; si `validate` falla, corrige la entidad o el script.

`datos_prueba.sql` debe insertar datos ficticios (ejecutable **después** de `crear_base_datos.sql`):
- 3 usuarios de desarrollo, uno por rol, con contraseña **`Demo1234!`** guardada como hash BCrypt real. Genera el hash de verdad (con un pequeño test de Java o código ejecutado) y **comprueba** que `BCryptPasswordEncoder.matches("Demo1234!", hash)` es verdadero; no inventes el hash.
  - `huesped@luxuriusroom.test`, `anfitrion@luxuriusroom.test`, `admin@luxuriusroom.test`.
- Al menos 8 alojamientos activos en al menos 4 ciudades distintas (por ejemplo Medellín, Cartagena, Bogotá, Cali), con tipos variados, 2 a 4 habitaciones cada uno (alguna con varias unidades), tarifas de temporada, servicios, 3 imágenes por alojamiento (URLs de demostración de `https://picsum.photos/seed/<texto>/1200/800`), un alojamiento en `PENDIENTE_APROBACION` y otro `SUSPENDIDO` para probar el panel admin.
- Algunas reservas de ejemplo en distintos estados (incluida alguna `COMPLETADA` con reseña y alguna `CANCELADA`) y reseñas de ejemplo con calificaciones coherentes con `calificacion_promedio`.
- Un comentario al inicio del archivo: **"SOLO PARA DESARROLLO. No ejecutar en producción."**

---

## 6. BACKEND (Spring Boot)

**Configuración (`application.properties`)** — sin contraseñas ni claves reales; todo por variables de entorno:

```
server.port=8080
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/luxurius_room?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
app.jwt.secret=${JWT_SECRET}
app.jwt.expiracion-minutos=120
app.cors.origenes=${CORS_ORIGINS:http://localhost:5500,http://127.0.0.1:5500}
```

`DB_PASSWORD` y `JWT_SECRET` **no tienen valor por defecto**: si faltan, la aplicación debe fallar al arrancar con un mensaje claro. `JWT_SECRET` debe tener al menos 32 caracteres (valídalo al arrancar).

**Seguridad (`config/`)**
- API sin estado (`STATELESS`), CSRF desactivado (se usa token Bearer, no cookies), CORS permitiendo los orígenes de `app.cors.origenes`, métodos GET/POST/PUT/PATCH/DELETE/OPTIONS y cabeceras `Authorization` y `Content-Type`.
- El token JWT incluye: `sub` = id del usuario, `email`, `rol`; expira según `app.jwt.expiracion-minutos`. Convierte `rol` en autoridad `ROLE_<ROL>`.
- Rutas públicas: `POST /api/auth/**`, `GET /api/alojamientos/**` (incluye habitaciones, disponibilidad y reseñas). Todo lo demás requiere token.
- `/api/anfitrion/**` solo `ANFITRION`; `/api/admin/**` solo `ADMINISTRADOR`; `/api/usuario/**` y reservas de cualquier usuario autenticado.
- Respuestas 401 (sin token o token inválido) y 403 (sin permiso) en JSON.
- Manejador global de errores con formato uniforme: `{ "estado": 400, "error": "Solicitud inválida", "mensaje": "...", "detalles": ["campo: motivo"] }`. Códigos: 400 validación, 401, 403, 404, 409 (conflicto: sin disponibilidad, transición inválida, email repetido), 500 sin exponer detalles internos.
- Nunca registres contraseñas ni tokens en logs.

**Endpoints mínimos** (todos devuelven JSON; las listas grandes aceptan `?pagina=0&tamano=12`):

| Método y ruta | Acceso | Descripción |
|---|---|---|
| POST `/api/auth/registro` | público | Crea cuenta (rol HUESPED o ANFITRION). Devuelve 201 |
| POST `/api/auth/login` | público | Devuelve `{ token, usuario:{id,nombre,email,rol} }` |
| GET `/api/alojamientos` | público | Búsqueda + filtros (ver RF01/RF02) |
| GET `/api/alojamientos/{id}` | público | Detalle completo |
| GET `/api/alojamientos/{id}/habitaciones` | público | Habitaciones y, si se envían fechas y huéspedes, su disponibilidad y precio total calculado |
| GET `/api/alojamientos/{id}/disponibilidad` | público | Fechas ocupadas/bloqueadas por habitación, para el calendario |
| GET `/api/alojamientos/{id}/resenas` | público | Reseñas visibles |
| POST `/api/reservas` | autenticado | Crea la reserva |
| GET `/api/reservas/{codigo}` | dueño, anfitrión del alojamiento o admin | Detalle |
| GET `/api/usuario/reservas` | autenticado | Reservas del usuario |
| PATCH `/api/reservas/{codigo}/cancelar` | dueño o anfitrión del alojamiento | Cancela |
| GET `/api/usuario/perfil` · PUT `/api/usuario/perfil` | autenticado | Consultar/editar datos (nombre, apellido, teléfono; cambio de contraseña pidiendo la actual) |
| POST `/api/resenas` | HUESPED | `{ codigoReserva, calificacion, comentario }` |
| POST `/api/anfitrion/alojamientos` | ANFITRION | Crea alojamiento (queda `PENDIENTE_APROBACION`) |
| PUT `/api/anfitrion/alojamientos/{id}` | ANFITRION dueño | Edita |
| GET `/api/anfitrion/alojamientos` | ANFITRION | Lista los propios |
| POST/PUT `/api/anfitrion/alojamientos/{id}/habitaciones` | ANFITRION dueño | Registrar/editar habitaciones |
| POST `/api/anfitrion/habitaciones/{id}/tarifas` | ANFITRION dueño | Definir tarifas |
| POST `/api/anfitrion/habitaciones/{id}/bloqueos` · DELETE `/api/anfitrion/bloqueos/{id}` | ANFITRION dueño | Bloquear/desbloquear fechas |
| POST `/api/anfitrion/alojamientos/{id}/imagenes` | ANFITRION dueño | Añadir foto por URL |
| PUT `/api/anfitrion/alojamientos/{id}/servicios` | ANFITRION dueño | Definir servicios |
| GET `/api/anfitrion/reservas` | ANFITRION | Reservas recibidas |
| PATCH `/api/anfitrion/reservas/{codigo}/confirmar` · `/completar` · `/no-presentado` · `/pago` | ANFITRION dueño | Cambios de estado según la sección 4 (`/pago` registra PAGADO_EN_SITIO) |
| POST `/api/anfitrion/resenas/{id}/reportar` | ANFITRION dueño | Reporta una reseña |
| GET `/api/admin/usuarios` · PATCH `/api/admin/usuarios/{id}/activo` | ADMINISTRADOR | Listar y activar/desactivar |
| GET `/api/admin/alojamientos` · PATCH `/api/admin/alojamientos/{id}/aprobar` · `/suspender` | ADMINISTRADOR | Moderación |
| GET `/api/admin/reservas` | ADMINISTRADOR | Todas las reservas |
| GET `/api/admin/estadisticas` | ADMINISTRADOR | Totales de usuarios, alojamientos por estado, reservas por estado, reservas del mes |
| GET `/api/admin/resenas/reportadas` · PATCH `/api/admin/resenas/{id}/ocultar` · `/restaurar` | ADMINISTRADOR | Gestión de reseñas |

**Validaciones (Bean Validation en los DTO + reglas en los `service`)**: email válido, contraseña de mínimo 8 caracteres con letras y números, campos obligatorios, rangos de fechas, límites de huéspedes, tamaños de texto. Los permisos y la propiedad de los recursos se verifican en el **servicio/backend**, nunca solo ocultando botones en el HTML.

Cada acción importante (crear/cancelar reserva, cambiar estado, aprobar/suspender, registrar pago en sitio, ocultar reseña) escribe una fila en `auditoria`.

---

## 7. FRONTEND (detalle por pantalla)

Configuración común en `app.js`: `const API_URL = "http://localhost:8080/api";`, una función `llamarApi(ruta, opciones)` que añade `Authorization: Bearer <token>` cuando existe, convierte errores JSON en mensajes legibles y, ante un 401, cierra sesión y redirige a `login.html`. El token y los datos básicos del usuario se guardan en `localStorage`. El navbar se genera según el rol.

Seguridad en el frontend: al mostrar texto que viene de la base de datos usa `textContent` (o escapa el HTML); **nunca** insertes datos del usuario con `innerHTML` sin escapar.

Pantallas:

- **index.html** — Hero con buscador: ubicación (ciudad), fecha de entrada, fecha de salida, adultos, niños, habitaciones. Validaciones en cliente (fechas, máximo 16 huéspedes) y envío a `alojamientos.html` con los parámetros en la URL. Debajo, algunos alojamientos destacados cargados desde la API.
- **alojamientos.html** — Resultados en tarjetas, con **contador de resultados**, panel de filtros: precio mínimo/máximo (valida mín ≤ máx), tipo, calificación mínima, Wi-Fi, estacionamiento, desayuno, aire acondicionado, capacidad. Los filtros funcionan combinados. Mensaje claro cuando no hay resultados. Paginación.
- **detalle-alojamiento.html** — Nombre, ubicación, galería de fotos, descripción, servicios, reglas, habitaciones con capacidad, camas, disponibilidad y precio por noche, calendario de ocupación (simple, hecho con HTML/JS puro, que marque días no disponibles), política de cancelación, calificación y reseñas, botón **"Reservar"** que abre un modal de reserva.
- **Flujo de reserva (modal en el detalle)**: elegir habitación → fechas → huéspedes y unidades → ver el precio total calculado por el servidor (desglose: noches, precio por noche, cargos adicionales, total) → casilla "Acepto las condiciones" → "Confirmar solicitud de reserva". Si no ha iniciado sesión, se redirige a `login.html` y luego se regresa. No hay ningún campo de tarjeta.
- **Confirmación**: muestra código de reserva, nombre del alojamiento, habitación, fechas de entrada y salida, noches, huéspedes, precio por noche, cargos adicionales, total estimado, estado de la reserva y estado del pago (**"Pendiente de pago en sitio"**), y este mensaje exacto:
  > "Tu reserva se gestionará a través de Luxurius Room. El pago se realizará directamente en el alojamiento, de acuerdo con las condiciones establecidas por el establecimiento."
- **login.html / registro.html** — Formularios con validación, mensajes de error de la API, selector de tipo de cuenta (Huésped / Anfitrión) en el registro. Cerrar sesión desde el navbar.
- **perfil.html** — Ver y editar datos personales; cambiar contraseña.
- **mis-reservas.html** — Lista de reservas con código, estado de reserva y estado de pago; botón cancelar (solo si procede); botón "Calificar" en reservas `COMPLETADA` sin reseña (modal con estrellas 1–5 y comentario).
- **panel-anfitrion.html** — Pestañas: *Mis alojamientos* (crear/editar, servicios, fotos por URL), *Habitaciones y tarifas*, *Bloqueos de fechas*, *Reservas recibidas* (confirmar, cancelar, completar, no presentado, registrar pago en sitio), *Reseñas* (reportar). Muestra el estado de aprobación de cada alojamiento.
- **panel-administrador.html** — Pestañas: *Estadísticas*, *Usuarios*, *Alojamientos* (aprobar/suspender), *Reservas*, *Reseñas reportadas* (ocultar/restaurar).
- Las páginas de panel y de reservas redirigen a `login.html` si no hay sesión o el rol no corresponde (además del control real en el backend).
- No debe haber botones que no hagan nada ni datos inventados presentados como reales. Cada acción debe llamar a la API y mostrar éxito o error.

---

## 8. SEGURIDAD (resumen para el README)

Documenta en el README, con palabras sencillas: hashing BCrypt, JWT con expiración, roles verificados en el backend, validación de datos, precios calculados en el servidor, protección contra doble reserva (bloqueo pesimista), CORS restringido al frontend, secretos en variables de entorno (nunca en el repositorio), consultas parametrizadas (JPA, sin concatenar SQL), escape de HTML en el frontend, y las limitaciones conocidas de esta versión (token en `localStorage`, sin límite de intentos de login, sin HTTPS en desarrollo, usuarios de prueba solo para desarrollo).

---

## 9. README.md (en español, para principiantes)

Pasos numerados, con comandos exactos para **Windows PowerShell**:

1. Instalar Java 21 (JDK) y verificar con `java -version`.
2. Instalar MySQL 8 (y opcionalmente MySQL Workbench) y recordar la contraseña de `root`.
3. Instalar Visual Studio Code y las extensiones recomendadas (*Extension Pack for Java*, *Spring Boot Extension Pack*, *Live Server*).
4. Abrir la carpeta `luxurius-room` en VS Code.
5. Crear la base de datos y ejecutar los scripts desde PowerShell, por ejemplo:
   `mysql -u root -p < database\crear_base_datos.sql` y `mysql -u root -p luxurius_room < database\datos_prueba.sql` (explica qué hacer si `mysql` no se reconoce: agregarlo al PATH o usar Workbench).
6. Configurar variables de entorno en la sesión de PowerShell:
   `$env:DB_PASSWORD="tu_clave"` · `$env:JWT_SECRET="una-clave-larga-de-al-menos-32-caracteres"` (explica cómo generar una).
7. Ejecutar el backend: `cd backend` y `.\mvnw.cmd spring-boot:run`. Indicar cómo saber que arrancó (mensaje `Started LuxuriusRoomApplication`, puerto 8080).
8. Abrir el frontend: con Live Server (clic derecho en `frontend/index.html` → *Open with Live Server*, puerto 5500) **o** con `cd frontend; python -m http.server 5500`. No abrir los HTML con doble clic (`file://`), porque CORS fallaría.
9. Comprobar la comunicación: abrir `http://localhost:8080/api/alojamientos` en el navegador y ver el JSON; abrir el frontend y ver alojamientos en la portada.
10. Probar endpoints con `Invoke-RestMethod` en PowerShell (ejemplos de login, búsqueda y creación de reserva con token).
11. Usuarios de prueba (correos y contraseña `Demo1234!`) y la advertencia de que son solo para desarrollo.
12. Solución de errores frecuentes: puerto 8080 ocupado, contraseña de MySQL incorrecta, `Schema-validation: missing table`, `JWT_SECRET` ausente, error de CORS, `java` no reconocido, `mysql` no reconocido, versión de Java incorrecta.

Incluye además: cómo ejecutar las pruebas (`.\mvnw.cmd test`), una descripción corta de la estructura del proyecto, la tabla de endpoints y la sección de seguridad.

---

## 10. PRUEBAS Y VERIFICACIÓN

Escribe pruebas automáticas para las reglas críticas:
- Cálculo de precio (tarifas por temporada, tarifa base, varias unidades, cargo de limpieza, redondeo).
- Validación de fechas (entrada pasada, salida ≤ entrada, 0 noches) y límite de 16 huéspedes.
- Solapamiento (incluido el caso salida-día-X / entrada-día-X, que **sí** es válido) y control de unidades en habitaciones con varias unidades.
- Una reserva nueva nace con `PENDIENTE_CONFIRMACION` y `PENDIENTE_EN_SITIO`; no se puede marcar pago sin cumplir las condiciones.
- Cancelación y liberación de disponibilidad; transiciones de estado inválidas devuelven 409.
- Un anfitrión no puede modificar alojamientos ajenos; un huésped no puede acceder a `/api/admin/**`.
- No se permite reseña duplicada ni reseña sobre una reserva no completada.
- El hash de la contraseña de prueba coincide con `Demo1234!`.

Verificación real (ejecútala tú, en la terminal, no la supongas):
1. `.\mvnw.cmd clean test` pasa sin errores (en Linux/macOS `./mvnw`).
2. El backend arranca y se conecta a MySQL con `ddl-auto=validate` sin errores.
3. Prueba con `curl`/`Invoke-RestMethod`: registro, login, búsqueda con filtros, detalle, reserva, solapamiento rechazado, cancelación, 403 para rol incorrecto.
4. Busca en el código y confirma que **no hay** referencias a pagos en línea, tarjetas ni pasarelas.
5. Revisa que cada archivo HTML cargue sus scripts y estilos con rutas correctas.

**Honestidad:** no afirmes que algo funciona si no creaste su código o no pudiste ejecutarlo. Si en tu entorno no hay MySQL o Java 21 y no puedes ejecutar algo, dilo claramente en un apartado "Pendiente de verificar" del README y en tu respuesta, y deja las instrucciones para que el usuario lo compruebe.

---

## 11. CÓMO TRABAJAR (instrucciones para la IA)

- Trabaja **directamente sobre los archivos** del proyecto y ejecuta comandos en la terminal; no te limites a mostrar código en el chat.
- Empieza inspeccionando la carpeta de trabajo y comprobando qué hay instalado (`java -version`, `mysql --version`). Si falta algo, dilo y continúa con lo que puedas hacer.
- Trabaja por etapas, en este orden. Al terminar cada etapa, compila/prueba, corrige errores **antes de avanzar**, y resume en 3–5 líneas lo hecho:
  1. Estructura de carpetas, `.gitignore`, `pom.xml`, Maven Wrapper.
  2. Base de datos (scripts SQL).
  3. Backend base: entidades, repositorios, conexión a MySQL, manejador de errores, configuración.
  4. Búsqueda, filtros y detalle de alojamientos (backend + frontend).
  5. Registro, login, perfil y seguridad JWT por roles.
  6. Sistema de reservas.
  7. Disponibilidad, tarifas y bloqueos.
  8. Panel del anfitrión.
  9. Panel del administrador.
  10. Reseñas.
  11. Diseño responsive y pulido visual con Bootstrap.
  12. Pruebas, README y verificación final.
- Si no alcanzas a terminar todo en una ejecución, indica exactamente en qué etapa te quedaste y continúa desde ahí sin borrar trabajo funcional.
- No crees endpoints vacíos, botones sin acción ni datos simulados presentados como reales (los datos de demostración deben estar claramente marcados como tales en el README).
- No sobrescribas archivos existentes sin leerlos antes.
- No añadas dependencias fuera de las permitidas en la sección 1. Si crees que hace falta una, explícalo y pregunta.

---

## 12. RESULTADO ESPERADO (criterios de "terminado")

El proyecto está terminado cuando, siguiendo solo el README en una máquina con Windows, Java 21 y MySQL:

- [ ] El backend compila, las pruebas pasan y arranca en el puerto 8080 conectado a MySQL.
- [ ] El frontend se abre en `http://localhost:5500` y muestra alojamientos reales de la base de datos.
- [ ] Un visitante puede buscar, filtrar y ver el detalle de un alojamiento.
- [ ] Un usuario puede registrarse, iniciar sesión, editar su perfil, reservar, recibir un código `LR-XXXXXXXX` y ver y cancelar sus reservas.
- [ ] No se puede reservar una habitación sin disponibilidad ni con fechas solapadas; el precio total lo calcula el servidor.
- [ ] El anfitrión administra sus alojamientos, habitaciones, tarifas y bloqueos, y gestiona las reservas (confirmar, cancelar, completar, no presentado, registrar pago en sitio).
- [ ] El administrador aprueba/suspende alojamientos, consulta usuarios, reservas y estadísticas, y modera reseñas reportadas.
- [ ] Un huésped puede reseñar una estancia completada, una sola vez.
- [ ] Los permisos se aplican en el backend (probado con tokens de distintos roles).
- [ ] No existe ninguna pasarela de pagos; el estado de pago permanece `PENDIENTE_EN_SITIO` hasta que el anfitrión registre el pago presencial.
- [ ] No hay contraseñas ni claves reales en ningún archivo del proyecto.

**No implementes pagos en línea. El pago se realizará presencialmente en el alojamiento.**

Prioriza la simplicidad, el aprendizaje, la seguridad y el funcionamiento real sobre la cantidad de tecnologías.

**Comienza ahora con la etapa 1 y continúa etapa por etapa.**

# FIN DEL PROMPT
