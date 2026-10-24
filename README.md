# DigitalFix - Microservicio de Catálogo (ms-catalog)

Microservicio encargado del CRUD de servicios técnicos, tarifas y stock de repuestos del sistema DigitalFix.

## Puerto

Puerto asignado en desarrollo local: `8088`, único dentro del ecosistema (ver la [tabla de puertos](https://github.com/SolgreyDuocUC/digitalfix-infrastructure#86-puertos-de-los-microservicios) en `digitalfix-infrastructure`).

- Ejecución local (`mvnw spring-boot:run` o IDE): variable `SERVER_PORT` del `.env` (por defecto `8088`).
- Docker Compose: el contenedor escucha en `8080` y se publica en `${APP_PORT:-8088}` del host.
- Swagger UI: `http://localhost:8088/swagger-ui.html`
