# Luxurius Room

Aplicación web para buscar alojamientos, solicitar reservas y administrar una plataforma de hospedaje. Usa Java 21, Spring Boot 3.3.13, MySQL 8 y archivos estáticos HTML/CSS/JavaScript.

> Los datos de demostración son solo para desarrollo. No ejecutar en producción.

## Requisitos

- Windows PowerShell 5.1 o PowerShell 7.
- JDK 21. Compruébalo con `java -version`.
- MySQL 8 y su cliente `mysql`.
- Visual Studio Code. Son recomendables *Extension Pack for Java*, *Spring Boot Extension Pack* y *Live Server*.
- Python solo es necesario si prefieres usar su servidor estático en lugar de Live Server.

## Preparación y ejecución

1. Instala Java 21 y verifica la versión:

   ```powershell
   java -version
   ```

2. Instala MySQL 8 y recuerda la contraseña local de `root`. Puedes usar MySQL Workbench para ejecutar scripts si `mysql` no está en el `PATH`.

3. Abre la carpeta `luxurius-room` en Visual Studio Code.

4. Desde PowerShell, ubicado en `luxurius-room`, crea la base y carga los datos de desarrollo:

   ```powershell
   cmd /c "mysql -u root -p < database\crear_base_datos.sql"
   cmd /c "mysql -u root -p luxurius_room < database\datos_prueba.sql"
   ```

   Ejecuta el segundo comando después del primero. El script de datos contiene cuentas y reservas ficticias; nunca lo ejecutes en producción.

5. Configura secretos en la sesión de PowerShell que usarás para el backend. `DB_PASSWORD` no tiene valor predeterminado; `JWT_SECRET` debe tener al menos 32 caracteres:

   ```powershell
   $env:DB_PASSWORD = "tu_clave_local_de_mysql"
   $secretBytes = New-Object byte[] 32
   [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($secretBytes)
   $env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
   ```

   No guardes estos valores en archivos del proyecto ni los compartas. `DB_URL`, `DB_USER` y `CORS_ORIGINS` son opcionales; consulta `backend/src/main/resources/application.properties`.

6. Inicia el backend:

   ```powershell
   Set-Location .\backend
   .\mvnw.cmd spring-boot:run
   ```

   Está listo cuando aparezca `Started LuxuriusRoomApplication`; escucha en el puerto `8080`. Mantén abierta esta terminal.

7. En otra terminal, abre el frontend con Live Server (clic derecho en `frontend/index.html` → *Open with Live Server*, puerto `5500`) o ejecuta:

   ```powershell
   Set-Location .\frontend
   python -m http.server 5500
   ```

   Abre `http://localhost:5500`. No abras las páginas mediante `file://`; el navegador necesita el origen HTTP permitido por CORS.

8. Comprueba la API:

   ```powershell
   Invoke-RestMethod http://localhost:8080/api/alojamientos
   ```

   Inicia sesión y usa el token para una búsqueda protegida o una cotización. Sustituye `habitacionId` por una habitación activa existente:

   ```powershell
   $credenciales = @{ email = "huesped@luxuriusroom.test"; password = "Demo1234!" } | ConvertTo-Json
   $sesion = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $credenciales
   $headers = @{ Authorization = "Bearer $($sesion.token)" }
   Invoke-RestMethod "http://localhost:8080/api/alojamientos?ciudad=Medellin&pagina=0&tamano=12"
   $cotizacion = @{
       habitacionId = 1
       fechaEntrada = (Get-Date).AddDays(10).ToString("yyyy-MM-dd")
       fechaSalida = (Get-Date).AddDays(12).ToString("yyyy-MM-dd")
       adultos = 2
       ninos = 0
       unidades = 1
       aceptoCondiciones = $false
   } | ConvertTo-Json
   Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/reservas/cotizacion" -Headers $headers -ContentType "application/json" -Body $cotizacion
   ```

## Cuentas de desarrollo

Las cuentas de prueba usan la contraseña `Demo1234!`:

- `huesped@luxuriusroom.test`
- `anfitrion@luxuriusroom.test`
- `admin@luxuriusroom.test`

No reutilices estas credenciales fuera del entorno local.

## Funciones

- Visitantes: búsqueda y filtros, detalle, disponibilidad por unidad y cotización de reservas calculada por el servidor.
- Huéspedes: registro, inicio de sesión, perfil, reservas, cancelación permitida y reseña de una estancia completada.
- Anfitriones: gestión de alojamientos, habitaciones, servicios, fotos, tarifas, bloqueos, reservas, pago presencial e informes de reseñas.
- Administradores: estadísticas, usuarios, moderación de alojamientos, consulta de reservas y gestión de reseñas reportadas.
- No hay pagos en línea ni se solicitan o guardan datos de tarjetas. El pago se registra presencialmente en el alojamiento.

Los paneles están en `frontend/panel-anfitrion.html` y `frontend/panel-administrador.html`. La interfaz muestra el panel según el rol, pero la autorización efectiva siempre se aplica en el backend.

## API principal

Las rutas protegidas requieren `Authorization: Bearer <token>`.

| Método | Ruta | Acceso | Uso |
|---|---|---|---|
| POST | `/api/auth/registro` | Público | Crear cuenta de huésped o anfitrión |
| POST | `/api/auth/login` | Público | Iniciar sesión y recibir JWT |
| GET | `/api/alojamientos` | Público | Buscar y filtrar alojamientos activos |
| GET | `/api/alojamientos/{id}` | Público | Consultar detalle |
| GET | `/api/alojamientos/{id}/habitaciones` | Público | Consultar habitaciones activas |
| GET | `/api/alojamientos/{id}/disponibilidad` | Público | Consultar reservas y bloqueos por habitación |
| GET | `/api/alojamientos/{id}/resenas` | Público | Consultar reseñas visibles |
| POST | `/api/reservas/cotizacion` | Huésped | Cotizar noches y unidades |
| POST | `/api/reservas` | Huésped | Crear solicitud de reserva |
| GET | `/api/usuario/reservas` | Usuario | Consultar las reservas propias |
| GET/PUT | `/api/usuario/perfil` | Usuario | Consultar y editar el perfil |
| PATCH | `/api/reservas/{codigo}/cancelar` | Dueño o anfitrión | Cancelar si la transición es válida |
| POST | `/api/resenas` | Huésped | Calificar una reserva completada |
| GET/POST | `/api/anfitrion/alojamientos` | Anfitrión | Listar y crear alojamientos propios |
| GET/PUT | `/api/anfitrion/alojamientos/{id}` | Anfitrión dueño | Consultar o editar un alojamiento propio |
| POST | `/api/anfitrion/alojamientos/{id}/habitaciones` | Anfitrión dueño | Crear habitación |
| PUT | `/api/anfitrion/alojamientos/{id}/habitaciones/{habitacionId}` | Anfitrión dueño | Editar habitación |
| PUT/POST | `/api/anfitrion/alojamientos/{id}/servicios`, `/api/anfitrion/alojamientos/{id}/imagenes` | Anfitrión dueño | Definir servicios y agregar foto por URL |
| POST | `/api/anfitrion/habitaciones/{id}/tarifas` | Anfitrión dueño | Crear tarifa con rango inclusivo |
| POST/DELETE | `/api/anfitrion/habitaciones/{id}/bloqueos`, `/api/anfitrion/bloqueos/{id}` | Anfitrión dueño | Bloquear o liberar unidades |
| GET/PATCH | `/api/anfitrion/reservas`, `/api/anfitrion/reservas/{codigo}/{accion}` | Anfitrión dueño | Gestionar reservas (`confirmar`, `cancelar`, `completar`, `no-presentado`, `pago`) |
| GET/POST | `/api/anfitrion/resenas`, `/api/anfitrion/resenas/{id}/reportar` | Anfitrión dueño | Consultar y reportar reseñas |
| GET/PATCH | `/api/admin/usuarios`, `/api/admin/usuarios/{id}/activo` | Administrador | Consultar y activar/desactivar cuentas |
| GET/PATCH | `/api/admin/alojamientos`, `/api/admin/alojamientos/{id}/aprobar`, `/suspender` | Administrador | Moderar alojamientos |
| GET | `/api/admin/reservas`, `/api/admin/estadisticas` | Administrador | Consultar reservas y estadísticas |
| GET/PATCH | `/api/admin/resenas/reportadas`, `/api/admin/resenas/{id}/ocultar`, `/restaurar` | Administrador | Gestionar reportes de reseñas |

Las listas públicas aceptan `pagina` y `tamano`.

## Pruebas

Desde `backend`:

```powershell
Set-Location .\backend
.\mvnw.cmd clean test
```

Las pruebas cubren autenticación y roles, precios, disponibilidad y solapamientos, transiciones de reserva, propiedad del anfitrión, reseñas, promedio y moderación. Las pruebas unitarias usan Mockito; la prueba de repositorio usa H2 con alcance `test`.

## Seguridad y límites conocidos

- Contraseñas con BCrypt y JWT firmado con HS256 y expiración.
- Los roles y la propiedad de alojamientos se verifican en el backend.
- Validación de entrada, consultas JPA parametrizadas y precios calculados en el servidor.
- La creación de reservas bloquea la fila de habitación para reducir reservas simultáneas de las mismas unidades.
- CORS se limita a los orígenes configurados. Secretos solo por variables de entorno.
- El frontend inserta texto de la API mediante `textContent` y conserva JWT en `localStorage`; esa opción tiene riesgo ante XSS.
- Esta versión no limita intentos de inicio de sesión y el desarrollo local no usa HTTPS.

## Solución de problemas

- Puerto 8080 ocupado: cierra el proceso que lo usa o configura otro puerto para el backend.
- Error de conexión MySQL: confirma que MySQL está iniciado, que la contraseña es correcta y que las tablas se crearon con `crear_base_datos.sql`.
- `Schema-validation: missing table`: vuelve a ejecutar el script de creación en `luxurius_room`.
- `DB_PASSWORD` o `JWT_SECRET` ausente: configúralos en la misma terminal que inicia Spring Boot.
- Error CORS: sirve el frontend en `http://localhost:5500` o agrega el origen exacto a `CORS_ORIGINS`.
- `java` o `mysql` no reconocido: agrega el directorio `bin` correspondiente al `PATH` o usa Workbench para los scripts.
- Java incorrecto: confirma que `java -version` muestre JDK 21.

## Pendiente de verificar

La última verificación limpia ejecutó **42 pruebas, 0 fallos** con Java 21 y Spring Boot 3.3.13. El navegador integrado cargó las pantallas de panel, pero no pudo probar las llamadas autenticadas porque el backend no estaba iniciado. El puerto local 3306 respondió, pero no hay cliente `mysql` disponible ni se verificó la autenticación; quedan pendientes el arranque real conectado a MySQL y los flujos HTTP con datos locales.