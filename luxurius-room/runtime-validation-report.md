# Runtime Validation Report

**Generated**: 2026-10-16T20:25:00-05:00  
**Target**: `luxurius-room`

## Summary

| Step | Status | Exit Code | Details |
|---|---|---:|---|
| Startup | UNVERIFIED | n/a | `spring-boot:run` no se ejecutó: faltan `DB_PASSWORD` y `JWT_SECRET`. Las peticiones del navegador al puerto 8080 fueron rechazadas. |
| Integration tests | PASS | 0 | `mvnw.cmd clean test` desde `backend`: 42 pruebas, 0 fallos, 0 errores. Incluye unit tests Mockito, una prueba de repositorio H2 y seguridad MockMvc. |
| Browser | PARTIAL | n/a | Las dos páginas de panel cargaron desde `http://localhost:5500`; las peticiones a la API no pudieron completarse porque el backend no estaba iniciado. |

**Overall**: NEEDS_SIGNOFF. La compilación limpia y la suite automatizada pasan; quedan pendientes el arranque MySQL y las pruebas HTTP autenticadas de extremo a extremo.

## Environment

- Docker: UNAVAILABLE; el comando `docker` no está instalado.
- Node.js: AVAILABLE; v24.18.0.
- MySQL CLI: UNAVAILABLE; el comando `mysql` no está instalado.
- MySQL TCP: puerto 3306 accesible, sin verificar autenticación.
- Credenciales: `DB_PASSWORD` y `JWT_SECRET` no estaban configuradas; no se imprimieron ni solicitaron secretos.
- Navegador integrado: disponible; permitió revisar las dos páginas de panel en un viewport compacto.

## Test Evidence

- Command: `mvnw.cmd clean test` desde `backend`.
- Exit code: `0`.
- Result: `42 tests, 0 failures, 0 errors, 0 skipped`.
- Surefire reports: `backend/target/surefire-reports/*.xml`.
- Los tests usan H2 únicamente en la prueba de repositorio; no equivalen a validar esquema o concurrencia contra MySQL.

## Browser Check

Se cargaron `panel-anfitrion.html` y `panel-administrador.html` desde el servidor estático en `http://localhost:5500`. Las pestañas y los formularios aparecen; el panel indica `Failed to fetch` porque la API en el puerto 8080 no estaba disponible. No se validaron operaciones autenticadas ni persistencia desde navegador.

## Known Gaps

- No se inició Spring Boot contra MySQL porque faltaban los secretos requeridos.
- No se probaron registro, login, búsqueda, reserva, cancelación ni respuestas 401/403/409 a través de HTTP real.
- No se probó concurrencia contra MySQL.
- Los tests H2 no verifican completamente las diferencias de dialecto y esquema de MySQL 8.
- No se instaló un runner Playwright con npm; se usó el navegador integrado solo para comprobar carga visual.