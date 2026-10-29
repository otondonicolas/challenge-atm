# Evidencias de pruebas

Entorno: Windows 11, JDK 20.0.1 (compilando con `--release 8`), Maven 3.9.8, Node.js 24 + Newman 6.2.1. Fecha: 2026-10-28.

## 1. Tests automatizados

### atm-service – `mvn test` → 46 tests, 0 fallos

```text
[INFO] Tests run: 3,  Failures: 0, Errors: 0 - AuditoriaTransaccionalIntegrationTest
[INFO] Tests run: 3,  Failures: 0, Errors: 0 - RegistroTransaccionTest
[INFO] Tests run: 10, Failures: 0, Errors: 0 - CuentaTest
[INFO] Tests run: 2,  Failures: 0, Errors: 0 - ConcurrenciaExtraccionTest
[INFO] Tests run: 4,  Failures: 0, Errors: 0 - CajeroServiceImplTest
[INFO] Tests run: 24, Failures: 0, Errors: 0 - CajeroControllerIntegrationTest
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Clase de test | Tipo | Qué valida |
|---|---|---|
| `CuentaTest` | Unitario | Débito, crédito, saldo en cero, saldo insuficiente sin modificar la cuenta, importes inválidos (0, negativos, 3 decimales, nulo) |
| `RegistroTransaccionTest` | Unitario | Enmascarado de tarjetas de 16 y 19 dígitos |
| `CajeroServiceImplTest` | Unitario (Mockito) | Se audita lo aprobado (con saldos), lo rechazado y los errores inesperados; las excepciones se propagan |
| `CajeroControllerIntegrationTest` | Integración (MockMvc + H2) | Los 4 endpoints con casos felices y de error, códigos HTTP, validación de formato, JSON mal formado, 404/405, propagación de `X-Request-Id`, health check |
| `ConcurrenciaExtraccionTest` | Integración / concurrencia | 20 hilos simultáneos: no hay saldos negativos ni actualizaciones perdidas |
| `AuditoriaTransaccionalIntegrationTest` | Integración | El archivo `transacciones.log` contiene el registro JSON correcto, también para operaciones rechazadas, y nunca el PAN completo |

### atm-cli – `mvn test` → 42 tests, 0 fallos

```text
[INFO] Tests run: 10, Failures: 0, Errors: 0 - AtmCliTest
[INFO] Tests run: 7,  Failures: 0, Errors: 0 - CajeroClientImplTest
[INFO] Tests run: 22, Failures: 0, Errors: 0 - ParserComandosTest
[INFO] Tests run: 3,  Failures: 0, Errors: 0 - FormatoMonedaTest
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Clase de test | Qué valida |
|---|---|
| `ParserComandosTest` | Parseo de las 4 operaciones, cantidad de parámetros, formato de tarjeta/CBU, importes válidos e inválidos |
| `FormatoMonedaTest` | Formato `$ 99999,99` |
| `AtmCliTest` | Mensajes exactos del enunciado, códigos de salida, que no se llame al servicio ante un uso incorrecto, mensaje de resultado incierto |
| `CajeroClientImplTest` | Contra un servidor HTTP real embebido: request JSON y header `X-Request-Id`, mapeo de errores, respuestas no JSON (502), servicio caído, **timeout de lectura en extracción = resultado incierto**, timeout en consulta = no incierto |

## 2. Prueba de que el test de concurrencia detecta el problema

Se quitó temporalmente `@Lock(LockModeType.PESSIMISTIC_WRITE)` de `CuentaRepository` y se corrió `ConcurrenciaExtraccionTest` 3 veces:

```text
== corrida 1
[ERROR] Tests run: 2, Failures: 2, Errors: 0, Skipped: 0
expected: 4500.00
 but was: 4900.00
expected: 10
 but was: 20          <- se aprobaron las 20 extracciones de $100 sobre una cuenta con $1000
== corrida 2
[ERROR] Tests run: 2, Failures: 2, Errors: 0, Skipped: 0
expected: 4500.00
 but was: 5050.00
expected: 10
 but was: 20
== corrida 3
[ERROR] Tests run: 2, Failures: 2, Errors: 0, Skipped: 0
expected: 4500.00
 but was: 4900.00
expected: 10
 but was: 20
```

Sin el bloqueo hay *lost updates* en las 3 corridas. Con el bloqueo restaurado los 2 tests pasan:

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## 3. Prueba end-to-end (servicio real + CLI)

Servicio levantado con `java -jar target/atm-service.jar`; health: `{"status":"UP","groups":["liveness","readiness"]}`.

```text
> atm login 4517660000000001
Ingreso exitoso
(exit code: 0)

> atm login 4517660000000003                      (tarjeta inactiva)
Ingreso no exitoso
(exit code: 1)

> atm login 4517669999999999                      (tarjeta inexistente)
Ingreso no exitoso
(exit code: 1)

> atm saldo 4517660000000001 1000000002
Su saldo es $ 2500,50
(exit code: 0)

> atm extraer 4517660000000001 1000000002 500,50
Retire su dinero
(exit code: 0)

> atm saldo 4517660000000001 1000000002
Su saldo es $ 2000,00
(exit code: 0)

> atm extraer 4517660000000001 1000000002 999999
Error: Saldo insuficiente
(exit code: 1)

> atm extraer 4517660000000001 1000000003 100      (cuenta de otra tarjeta)
Error: La cuenta no corresponde a la tarjeta
(exit code: 1)

> atm extraer 4517660000000003 1000000004 100      (tarjeta inactiva)
Error: La tarjeta se encuentra inactiva
(exit code: 1)

> atm depositar 4517660000000001 0170001100000000000003 1500.75   (cuenta de un tercero)
Depósito exitoso
(exit code: 0)

> atm saldo 4517660000000002 1000000003           (el tercero ve el depósito)
Su saldo es $ 11500,75
(exit code: 0)

> atm depositar 4517660000000001 0170001199999999999999 100
Error: No existe una cuenta con el CBU informado
(exit code: 1)

> atm saldo 4517660000000001 1000000003
Error: La cuenta no corresponde a la tarjeta
(exit code: 1)

> atm extraer 4517660000000001 1000000002 -50
Error: importe invalido '-50'. Use el formato 1500 o 1500,50
Uso:
  atm login <tarjeta>
  atm extraer <tarjeta> <cuenta> <importe>
  atm depositar <tarjeta> <cbu> <importe>
  atm saldo <tarjeta> <cuenta>
(exit code: 2)

> atm extraer 4517660000000001 1000000002 10.555
Error: importe invalido '10.555'. Use el formato 1500 o 1500,50
(exit code: 2)

> atm depositar 4517660000000001 123 100
Error: el CBU debe tener 22 digitos
(exit code: 2)

> atm transferir 4517660000000001
Error: operacion desconocida 'transferir'
(exit code: 2)

> atm extraer 4517660000000001 1000000002
Error: cantidad de parametros incorrecta. Uso: atm extraer <tarjeta> <cuenta> <importe>
(exit code: 2)
```

En los errores de uso (exit code 2) el CLI imprime siempre el bloque `Uso:`; arriba se muestra completo sólo en el primero.

### Servicio detenido

```text
> atm login 4517660000000001
Ingreso no exitoso
Error: El servicio no se encuentra disponible. Intente nuevamente mas tarde
(exit code: 3)

> atm extraer 4517660000000001 1000000001 100
Error: El servicio no se encuentra disponible. Intente nuevamente mas tarde
(exit code: 3)
```

### Llamada directa con curl y correlación por `X-Request-Id`

```text
$ curl -i -X POST localhost:8080/api/v1/cajero/saldo -H "Content-Type: application/json" \
       -H "X-Request-Id: demo-curl-1" -d '{"tarjeta":"4517660000000001","cuenta":"1000000001"}'
HTTP/1.1 200
X-Request-Id: demo-curl-1
{"transaccionId":"766bc0a7-a9ee-44c6-bf36-5b2823658a0f","cuenta":"1000000001","saldo":150000.00}
```

## 4. Pruebas de la API con Postman (Newman)

Colección [`postman/challenge-atm.postman_collection.json`](../postman/challenge-atm.postman_collection.json): 27 requests en 6 carpetas (estado del servicio, login, consulta de saldo, extracción, depósito y validaciones de entrada). Cada request valida el status HTTP y el código o mensaje esperado. Los flujos de extracción y depósito consultan el saldo antes y después y verifican que se haya debitado o acreditado exactamente el importe.

```text
$ npx newman run postman/challenge-atm.postman_collection.json

┌─────────────────────────┬─────────────────┬─────────────────┐
│                         │        executed │          failed │
├─────────────────────────┼─────────────────┼─────────────────┤
│              iterations │               1 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│                requests │              27 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│            test-scripts │              27 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│      prerequest-scripts │               0 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│              assertions │              54 │               0 │
├─────────────────────────┴─────────────────┴─────────────────┤
│ total run duration: 2.4s                                    │
├─────────────────────────────────────────────────────────────┤
│ total data received: 2.42kB (approx)                        │
├─────────────────────────────────────────────────────────────┤
│ average response time: 6ms [min: 4ms, max: 26ms, s.d.: 5ms] │
└─────────────────────────────────────────────────────────────┘
```

| Carpeta | Casos | Resultado esperado y obtenido |
|---|---|---|
| 0. Estado del servicio | Health check | 200, `UP` |
| 1. Login | Tarjeta activa, inactiva, inexistente | 200 `Ingreso exitoso` (con `X-Request-Id` devuelto), 403 `TARJETA_INACTIVA`, 401 `TARJETA_INEXISTENTE` |
| 2. Consulta de saldo | OK, cuenta de otra tarjeta, tarjeta inactiva | 200 con saldo, 403 `CUENTA_NO_ASOCIADA`, 403 `TARJETA_INACTIVA` |
| 3. Extracción | Saldo antes → extracción de $500,50 → saldo después; saldo insuficiente, cuenta ajena, tarjeta inactiva, importe 0, 3 decimales | 200 `Retire su dinero` y saldo exactamente $500,50 menor; 422, 403, 403, 400, 400 |
| 4. Depósito | Saldo destino antes → depósito de $1500 a un tercero → saldo destino después; CBU inexistente, tarjeta inactiva, importe negativo | 200 `Depósito exitoso` y saldo exactamente $1500 mayor; 404, 403, 400 |
| 5. Validaciones de entrada | Tarjeta y CBU con formato inválido, body vacío, JSON mal formado, endpoint inexistente, GET | 400 `DATOS_INVALIDOS` (con detalle por campo), 404 `RECURSO_INEXISTENTE`, 405 |

La misma batería está en [`postman/curls.sh`](../postman/curls.sh) como comandos `curl`.

## 5. Log transaccional generado (`logs/transacciones.log`)

Extracto real de la corrida de la sección 3:

```json
{"fechaHora":"2026-10-28T23:08:51.0466293-03:00","transaccionId":"73aab64c-a7ad-4e55-8f47-068704b1cd11","requestId":"9e2711e4-820c-47bd-b45e-6df6f9f40c05","operacion":"LOGIN","resultado":"APROBADA","tarjeta":"451766******0001"}
{"fechaHora":"2026-10-28T23:08:51.49037-03:00","transaccionId":"a436faa5-3232-449d-b1a5-ff8ca2402679","requestId":"63b440a3-aef6-45f6-9712-01a178ce8d0c","operacion":"LOGIN","resultado":"RECHAZADA","codigoError":"TARJETA_INACTIVA","tarjeta":"451766******0003"}
{"fechaHora":"2026-10-28T23:08:52.8017339-03:00","transaccionId":"3faaf37f-c680-4966-aeff-7ed3a163ec6a","requestId":"4089d155-9258-4f3b-8753-b312d221f736","operacion":"EXTRACCION","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000002","importe":500.50,"saldoAnterior":2500.50,"saldoPosterior":2000.00}
{"fechaHora":"2026-10-28T23:08:53.6260881-03:00","transaccionId":"67be0c44-baad-487e-a2d9-d2b4c35737f3","requestId":"1a6ef3bf-6c0c-4060-a03a-7fc37674539a","operacion":"EXTRACCION","resultado":"RECHAZADA","codigoError":"SALDO_INSUFICIENTE","tarjeta":"451766******0001","cuenta":"1000000002","importe":999999}
{"fechaHora":"2026-10-28T23:08:54.8727807-03:00","transaccionId":"3df3b733-fd93-4c2d-9602-967dd15ea8ef","requestId":"0d92c651-f521-4dce-8787-586318dbd654","operacion":"DEPOSITO","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000003","cbu":"0170001100000000000003","importe":1500.75,"saldoAnterior":10000.00,"saldoPosterior":11500.75}
{"fechaHora":"2026-10-28T23:08:55.6798269-03:00","transaccionId":"7ed5e947-9ff0-4f09-822d-165f76bd5099","requestId":"c95af80b-0756-4646-bcaf-fb0961a201ed","operacion":"DEPOSITO","resultado":"RECHAZADA","codigoError":"CBU_INEXISTENTE","tarjeta":"451766******0001","cbu":"0170001199999999999999","importe":100}
{"fechaHora":"2026-10-28T23:08:57.6883052-03:00","transaccionId":"766bc0a7-a9ee-44c6-bf36-5b2823658a0f","requestId":"demo-curl-1","operacion":"CONSULTA_SALDO","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000001","saldoAnterior":150000.00,"saldoPosterior":150000.00}
```

Verificación de que el PAN completo no aparece en ningún log:

```text
$ grep -c "4517660000000001" logs/*.log
logs/atm-service.log:0
logs/transacciones.log:0
```

Con estos registros se puede reconstruir el saldo de cada cuenta: el `saldoPosterior` de un movimiento coincide con el `saldoAnterior` del siguiente movimiento de esa misma cuenta (2500,50 → 2000,00 en la cuenta `1000000002`). El `requestId` `demo-curl-1` enviado por curl aparece en el registro, lo que permite correlacionar una llamada del cliente con su auditoría.
