# Producto ATM – Challenge Java

Cajero automático simplificado compuesto por dos proyectos independientes dentro del mismo repositorio:

| Proyecto | Descripción | Tecnología |
|---|---|---|
| [`atm-service`](atm-service) | Microservicio REST que gestiona tarjetas, cuentas y movimientos, y deja un log transaccional auditable. | Java 8, Spring Boot 2.7, Spring Data JPA, H2 en memoria |
| [`atm-cli`](atm-cli) | Aplicación de consola que ejecuta las 4 operaciones consumiendo el microservicio por HTTP. | Java 8, `HttpURLConnection`, Jackson |

Documentación adicional:

- [Documento técnico de diseño](docs/DISENO_TECNICO.md)
- [Evidencias de pruebas](docs/EVIDENCIAS_PRUEBAS.md)

**Tiempo invertido:** 2 horas aproximadamente.

---

## Requisitos

- JDK 8 o superior (el código compila con `--release 8`, por lo que sólo usa la API de Java 8).
- Maven 3.6 o superior.
- Puerto `8080` libre (configurable).

## Compilar y testear

```bash
cd atm-service
mvn clean package
```

```bash
cd atm-cli
mvn clean package
```

`mvn package` corre todos los tests (unitarios, de integración y de concurrencia). Para correr sólo los tests: `mvn test`.

## Ejecutar el microservicio

```bash
cd atm-service
java -jar target/atm-service.jar
```

También se puede levantar con `mvn spring-boot:run`.

| Variable de entorno | Default | Descripción |
|---|---|---|
| `ATM_SERVICE_PORT` | `8080` | Puerto HTTP |
| `ATM_LOG_DIR` | `logs` | Carpeta de logs (aplicación y transaccional) |

Health check: `GET http://localhost:8080/actuator/health`

Los datos se cargan en memoria al iniciar ([`schema.sql`](atm-service/src/main/resources/schema.sql) y [`data.sql`](atm-service/src/main/resources/data.sql)) y se reinician en cada arranque.

## Ejecutar la aplicación de consola

Con el servicio levantado:

```bash
cd atm-cli
atm login <tarjeta>
atm extraer <tarjeta> <cuenta> <importe>
atm depositar <tarjeta> <cbu> <importe>
atm saldo <tarjeta> <cuenta>
```

En Windows `atm` es [`atm.cmd`](atm-cli/atm.cmd); en Linux/macOS usar [`./atm.sh`](atm-cli/atm.sh). Ambos equivalen a `java -jar target/atm.jar ...`.

El importe acepta coma o punto decimal (`1500`, `1500,50`, `1500.50`), con hasta 2 decimales.

| Configuración | Propiedad (`-D`) | Variable de entorno | Default |
|---|---|---|---|
| URL del servicio | `atm.url` | `ATM_SERVICE_URL` | `http://localhost:8080` |
| Timeout de conexión | `atm.connect-timeout-ms` | `ATM_CONNECT_TIMEOUT_MS` | `3000` |
| Timeout de lectura | `atm.read-timeout-ms` | `ATM_READ_TIMEOUT_MS` | `10000` |

Códigos de salida: `0` éxito, `1` operación rechazada, `2` uso incorrecto, `3` servicio no disponible.

### Ejemplo

```text
> atm login 4517660000000001
Ingreso exitoso
> atm saldo 4517660000000001 1000000002
Su saldo es $ 2500,50
> atm extraer 4517660000000001 1000000002 500,50
Retire su dinero
> atm depositar 4517660000000001 0170001100000000000003 1500.75
Depósito exitoso
> atm extraer 4517660000000001 1000000002 999999
Error: Saldo insuficiente
```

## Datos de prueba

| Tarjeta | Activa | Cuentas asociadas |
|---|---|---|
| `4517660000000001` | S | `1000000001`, `1000000002` |
| `4517660000000002` | S | `1000000003` |
| `4517660000000003` | N | `1000000004` |

| Cuenta | CBU | Saldo inicial | Observación |
|---|---|---|---|
| `1000000001` | `0170001100000000000001` | 150000,00 | |
| `1000000002` | `0170001100000000000002` | 2500,50 | |
| `1000000003` | `0170001100000000000003` | 10000,00 | Útil para depósitos a terceros desde la tarjeta `...0001` |
| `1000000004` | `0170001100000000000004` | 50000,00 | Asociada a la tarjeta inactiva |
| `1000000005` | `0170001100000000000005` | 0,00 | Sin tarjeta asociada |

## API REST

Base: `http://localhost:8080/api/v1/cajero`. Todas las operaciones son `POST` con cuerpo JSON (los números de tarjeta nunca viajan en la URL). El header opcional `X-Request-Id` se propaga a los logs y se devuelve en la respuesta.

| Operación | Endpoint | Cuerpo | Respuesta OK |
|---|---|---|---|
| Login | `/login` | `{"tarjeta"}` | `{"transaccionId","mensaje":"Ingreso exitoso"}` |
| Extracción | `/extracciones` | `{"tarjeta","cuenta","importe"}` | `{"transaccionId","mensaje":"Retire su dinero"}` |
| Depósito | `/depositos` | `{"tarjeta","cbu","importe"}` | `{"transaccionId","mensaje":"Depósito exitoso"}` |
| Saldo | `/saldo` | `{"tarjeta","cuenta"}` | `{"transaccionId","cuenta","saldo"}` |

Errores: `{"codigo","mensaje","detalles"?}`.

| Código | HTTP |
|---|---|
| `TARJETA_INEXISTENTE` | 401 |
| `TARJETA_INACTIVA`, `CUENTA_NO_ASOCIADA` | 403 |
| `CBU_INEXISTENTE`, `RECURSO_INEXISTENTE` | 404 |
| `SALDO_INSUFICIENTE` | 422 |
| `IMPORTE_INVALIDO`, `DATOS_INVALIDOS` | 400 |
| `ERROR_INTERNO` | 500 |

```bash
curl -X POST http://localhost:8080/api/v1/cajero/extracciones \
  -H "Content-Type: application/json" \
  -d '{"tarjeta":"4517660000000001","cuenta":"1000000002","importe":100.50}'
```

## Probar la API con Postman o curl

En [`postman/`](postman) está todo lo necesario para probar el servicio levantado en `localhost:8080`:

| Archivo | Contenido |
|---|---|
| [`challenge-atm.postman_collection.json`](postman/challenge-atm.postman_collection.json) | Colección con 27 requests en 6 carpetas: estado, login, saldo, extracción, depósito y validaciones de entrada. Cada request tiene tests que validan el status y el código o mensaje esperado. |
| [`localhost.postman_environment.json`](postman/localhost.postman_environment.json) | Environment con `baseUrl = http://localhost:8080`. |
| [`curls.sh`](postman/curls.sh) | Los mismos casos como comandos `curl`, con el resultado esperado de cada uno. |

En Postman: **Import** → arrastrar los dos `.json` → elegir el environment **ATM - localhost** → **Run collection**.

Desde la terminal, sin Postman:

```bash
npx newman run postman/challenge-atm.postman_collection.json
```

```bash
sh postman/curls.sh
```

Los flujos de extracción y depósito comparan el saldo antes y después de la operación, así que la colección se puede correr varias veces sin reiniciar el servicio.

## Log transaccional

Archivo `atm-service/logs/transacciones.log` (rotación diaria). Una línea JSON por operación, aprobada o rechazada:

```json
{"fechaHora":"2026-10-16T20:01:35.85-03:00","transaccionId":"8c4dcca7-...","requestId":"c7505d18-...","operacion":"EXTRACCION","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000002","importe":500.50,"saldoAnterior":2500.50,"saldoPosterior":2000.00}
```

El detalle del formato está en el [documento técnico](docs/DISENO_TECNICO.md#log-transaccional).
