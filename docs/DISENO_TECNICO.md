# Documento técnico – Producto ATM

## 1. Arquitectura

```text
┌──────────────┐   HTTP/JSON (POST)    ┌──────────────────────────────────────────────┐
│   atm-cli    │ ────────────────────▶ │                 atm-service                  │
│ (consola)    │   X-Request-Id        │                                              │
│              │                       │  RequestIdFilter ─▶ CajeroController         │
│ ParserComandos                       │                        │ (Bean Validation)    │
│ HttpCajeroClient                     │                        ▼                      │
│ AtmCli       │                       │                  CajeroService ──▶ Auditoria │
└──────────────┘                       │                 (fachada + auditoría)  │      │
                                       │                        ▼               ▼      │
                                       │                MovimientoService   transacciones.log
                                       │                 (@Transactional)              │
                                       │                        ▼                      │
                                       │        Repositorios JPA ─▶ H2 en memoria       │
                                       └──────────────────────────────────────────────┘
```

Dos proyectos Maven independientes en el mismo repositorio, como pide el enunciado. No comparten código: el contrato entre ambos es la API HTTP.

### atm-service (capas)

| Paquete | Responsabilidad | Clases |
|---|---|---|
| `web` | Entrada HTTP, validación de formato, traducción de errores a HTTP, correlación | `CajeroController`, `ManejadorErrores`, `RequestIdFilter`, `dto/*` |
| `service` | Casos de uso. `CajeroService` orquesta y audita; `MovimientoService` ejecuta la lógica transaccional | `CajeroService`, `MovimientoService` |
| `domain` | Entidades del enunciado y reglas de negocio de la cuenta | `Tarjeta`, `Cuenta`, `TarjetaCuenta` |
| `repository` | Acceso a datos (Spring Data JPA) con bloqueo pesimista | `CuentaRepository`, `TarjetaRepository`, `TarjetaCuentaRepository` |
| `audit` | Log transaccional | `AuditoriaTransaccional`, `RegistroTransaccion` |
| `exception` | Catálogo de errores de negocio | `CodigoError`, `OperacionRechazadaException` |

### atm-cli

| Clase | Responsabilidad |
|---|---|
| `AtmCli` | Punto de entrada; traduce resultados a los mensajes del enunciado y a códigos de salida |
| `ParserComandos` / `TipoComando` / `Comando` | Parseo y validación local de argumentos (falla rápido sin llamar al servicio) |
| `CajeroClient` / `HttpCajeroClient` | Contrato e implementación HTTP del cliente; manejo de timeouts y errores |
| `FormatoMoneda` | Formato `$ 99999,99` |
| `Configuracion` | URL y timeouts por propiedad de sistema o variable de entorno |

## 2. Decisiones de diseño

### Tecnología
- **Java 8 en ambos proyectos.** Se compila con `maven.compiler.release=8`, que además de generar bytecode 8 impide usar APIs posteriores.
- **Backend en Java con Spring Boot 2.7.18** (última línea compatible con Java 8). El enunciado permitía otra tecnología, pero usar Java en ambos lados simplifica la ejecución y es el stack estándar para este tipo de servicio.
- **H2 en memoria con JPA**, en lugar de un `Map` hardcodeado: el enunciado permite persistencia en memoria, y una base relacional aporta transacciones ACID, bloqueos de fila y constraints. Pasar a Oracle/PostgreSQL es cambiar el datasource.
- **CLI sin frameworks**: `HttpURLConnection` (Java 8 no tiene `HttpClient`) + Jackson. Se empaqueta como fat-jar (`maven-shade-plugin`) para ejecutarlo con `java -jar`.

### Modelo de datos
Se respetan las entidades del enunciado:

```sql
tarjeta(numero PK, activa CHAR(1) CHECK IN ('S','N'))
cuenta(numero PK, cbu UNIQUE, saldo DECIMAL(15,2) CHECK (saldo >= 0))
tarjeta_cuenta(tarjeta FK, cuenta FK, PK(tarjeta, cuenta))
```

- **Dinero con `BigDecimal` / `DECIMAL(15,2)`**, nunca `double`, para evitar errores de redondeo.
- **El saldo no negativo se valida dos veces**: en el dominio (`Cuenta.debitar`) y con un `CHECK` en la base. Aunque un bug saltee la regla de negocio, la base rechaza el saldo negativo.
- El esquema lo define `schema.sql` (`ddl-auto=none`): el esquema es explícito y versionable, no lo genera Hibernate.

### API REST
- Recursos bajo `/api/v1/cajero` (versionado desde el inicio).
- **Todas las operaciones son `POST` con cuerpo JSON**, también la consulta de saldo: el número de tarjeta es un dato sensible y no debe quedar en URLs, que terminan en logs de proxies y servidores.
- Errores con forma uniforme `{codigo, mensaje, detalles}` y status HTTP semántico (ver README).

### Reglas de negocio por operación

| Operación | Validaciones (en orden) |
|---|---|
| Login | La tarjeta existe → está activa |
| Extracción | Tarjeta activa → la cuenta está asociada a la tarjeta (`TarjetaCuenta`) → importe válido → saldo suficiente |
| Depósito | Tarjeta activa → existe una cuenta con ese CBU (puede ser de un tercero) → importe válido |
| Saldo | Tarjeta activa → la cuenta está asociada a la tarjeta |

- Importe válido: mayor a cero y con hasta 2 decimales. La regla vive en el dominio (`Cuenta.validarImporte`), así los intentos inválidos también quedan auditados.
- Para extracción y saldo primero se verifica la asociación tarjeta-cuenta y después se lee la cuenta. Así una tarjeta no puede averiguar si existe una cuenta ajena: siempre recibe `CUENTA_NO_ASOCIADA`.

### Concurrencia y consistencia (servicio crítico)
- `MovimientoService` es `@Transactional`: cada operación es atómica y, si falla, se hace rollback.
- **Bloqueo pesimista** (`SELECT ... FOR UPDATE`, `@Lock(PESSIMISTIC_WRITE)` en `CuentaRepository`) al leer la cuenta que se va a modificar. Dos extracciones simultáneas sobre la misma cuenta se serializan, sin *lost updates* ni saldos negativos.
- Se eligió bloqueo pesimista y no optimista (`@Version`) porque en operaciones monetarias sobre una misma cuenta se prefiere esperar unos milisegundos a rechazar y reintentar. Además, el reintento automático de un débito es riesgoso.
- `ConcurrenciaExtraccionTest` lo demuestra: 20 extracciones simultáneas de $100 sobre una cuenta con $1000 aprueban exactamente 10 y la cuenta queda en $0. **Sin el lock el test falla** (se aprueban más de 10 y el saldo final es incorrecto); está registrado en las evidencias.

### Disponibilidad
- `server.shutdown=graceful`: al detener el servicio, las transacciones en curso terminan antes de cerrar.
- Endpoint `/actuator/health` con probes de liveness/readiness, para orquestadores o balanceadores.
- Pool de conexiones Hikari dimensionado y `LOCK_TIMEOUT` explícito en la base.
- El servicio es stateless (no hay sesión en memoria), así que escala horizontalmente detrás de un balanceador una vez que se reemplaza H2 por una base compartida.
- En el cliente: **timeouts de conexión y de lectura**, y diferenciación entre:
  - **Servicio no disponible** (no se pudo conectar): es seguro reintentar.
  - **Resultado incierto** (la solicitud de extracción/depósito se envió pero no llegó la respuesta): el cliente **no reintenta** y le pide al usuario consultar el saldo, informando el `X-Request-Id` como referencia. Reintentar a ciegas podría debitar dos veces.

### Log transaccional
- Requisito: trazabilidad completa para auditar saldos. Implementación: `AuditoriaTransaccional` escribe en el logger dedicado `AUDITORIA_TRANSACCIONAL`, configurado en `logback-spring.xml` con un appender propio (`logs/transacciones.log`), rotación diaria, retención de 10 años, `immediateFlush` y sin propagación al log de aplicación.
- **Formato: JSON Lines** (un objeto JSON por línea). Lo puede leer una persona, se parsea sin ambigüedad, admite agregar campos sin romper lectores existentes y lo ingieren directamente herramientas como ELK, Splunk o Datadog.

| Campo | Descripción |
|---|---|
| `fechaHora` | ISO-8601 con offset |
| `transaccionId` | UUID generado por el servidor; también se devuelve al cliente |
| `requestId` | Correlación con el cliente (`X-Request-Id`) y con el log de aplicación |
| `operacion` | `LOGIN`, `EXTRACCION`, `DEPOSITO`, `CONSULTA_SALDO` |
| `resultado` | `APROBADA`, `RECHAZADA`, `ERROR` |
| `codigoError` | Motivo del rechazo |
| `tarjeta` | **Enmascarada** (BIN + últimos 4, criterio PCI-DSS) |
| `cuenta`, `cbu`, `importe` | Datos de la operación |
| `saldoAnterior`, `saldoPosterior` | Permiten reconstruir y auditar el saldo de cada cuenta |

- Se registran **todas** las operaciones que llegan a la lógica de negocio, también las rechazadas y las fallidas, porque los intentos fallidos también son relevantes para auditoría y fraude.
- **El registro se escribe después del commit.** `CajeroService` invoca a `MovimientoService` (transaccional) y audita cuando la transacción ya se confirmó. Así el log nunca muestra como aprobada una operación que terminó en rollback.

## 3. Cómo se cumplen los requerimientos

| Requerimiento | Implementación |
|---|---|
| CLI con 4 operaciones por línea de comando | `AtmCli` + `ParserComandos`: `atm login/extraer/depositar/saldo` |
| Mensajes "Ingreso exitoso / no exitoso", "Retire su dinero", "Depósito exitoso", "Su saldo es $ 99999,99" | `AtmCli.ejecutar`, `FormatoMoneda` |
| Consumir el servicio por HTTP | `HttpCajeroClient` |
| Login: la tarjeta existe y está activa | `MovimientoService.validarTarjetaActiva` |
| Extraer: debitar sin saldo negativo | `Cuenta.debitar` + `CHECK (saldo >= 0)` + lock pesimista |
| Depositar en el CBU si existe (propio o de terceros) | `MovimientoService.depositar` / `findByCbuParaActualizar` |
| Consultar saldo | `MovimientoService.consultarSaldo` |
| Entidades Tarjeta, Cuenta, TarjetaCuenta | `domain/*`, `schema.sql` |
| Servicio crítico | Transacciones, locks, health checks, graceful shutdown, timeouts y manejo de resultado incierto en el cliente |
| Trazabilidad / log transaccional en archivo de texto | `AuditoriaTransaccional` → `logs/transacciones.log` (JSON Lines) |
| Java 8, localhost, datos en memoria | `release 8`, `localhost:8080`, H2 en memoria con `data.sql` |
| Proyectos separados en el mismo repo | `atm-service/`, `atm-cli/` |

## 4. Concesiones y supuestos

| Tema | Decisión | Motivo / alternativa productiva |
|---|---|---|
| Sesión de login | **No hay sesión**: cada operación vuelve a validar la tarjeta. | Cada comando del CLI es un proceso independiente, como pide el enunciado. En producción el login emitiría un token de corta duración (JWT) validado en cada operación. |
| Autenticación | Sólo número de tarjeta, según el enunciado. | En un ATM real se agrega PIN (validado en HSM), y mTLS entre terminal y servidor. |
| Comando `saldo` | Recibe `<tarjeta> <cuenta>`, como el modo de uso del enunciado. | El texto dice "sólo con el número de tarjeta", pero el ejemplo incluye la cuenta. Se siguió el ejemplo porque una tarjeta puede tener varias cuentas. |
| Persistencia | H2 en memoria; los datos se reinician en cada arranque. | Permitido por el enunciado. En producción, base relacional externa con migraciones (Flyway/Liquibase). |
| Auditoría vs. commit | Se escribe después del commit. Si el proceso cae justo entre el commit y la escritura, se perdería ese registro. | Para garantía total: patrón *outbox* (registrar el movimiento en una tabla dentro de la misma transacción y publicar al log de forma asíncrona). |
| Solicitudes mal formadas | Las rechaza Bean Validation (400) y quedan en el log de aplicación, no en el transaccional. | No llegan a la lógica de negocio ni tocan saldos. |
| Idempotencia | El cliente no reintenta débitos y advierte al usuario ante un resultado incierto. | Lo ideal es una *idempotency key* persistida en el servidor para poder reintentar sin duplicar. |
| Límites | No hay límite de extracción diaria ni por operación. | No estaba especificado; sería configuración por producto. |
| Seguridad del transporte | HTTP plano en localhost. | En producción, HTTPS/mTLS. |
| Formato de importe en CLI | Acepta `1500`, `1500,50` o `1500.50`; no acepta separador de miles. | Evita ambigüedad entre `1.500` y `1,500`. |
| Codificación de consola | Los acentos se muestran bien en una consola interactiva. Si la salida se redirige o captura, puede aparecer `Dep?sito` según la página de códigos. | Depende de la terminal, no de la aplicación. |
