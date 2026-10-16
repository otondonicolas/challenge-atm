# Evidencias de pruebas

Entorno: Windows 11, JDK 20.0.1 (compilando con `--release 8`), Maven 3.9.8. Fecha: 2026-10-16.

## 1. Tests automatizados

### atm-service – `mvn test` → 46 tests, 0 fallos

```text
[INFO] Tests run: 3,  Failures: 0, Errors: 0 - AuditoriaTransaccionalIntegrationTest
[INFO] Tests run: 3,  Failures: 0, Errors: 0 - RegistroTransaccionTest
[INFO] Tests run: 10, Failures: 0, Errors: 0 - CuentaTest
[INFO] Tests run: 4,  Failures: 0, Errors: 0 - CajeroServiceTest
[INFO] Tests run: 2,  Failures: 0, Errors: 0 - ConcurrenciaExtraccionTest
[INFO] Tests run: 24, Failures: 0, Errors: 0 - CajeroControllerIntegrationTest
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Clase de test | Tipo | Qué valida |
|---|---|---|
| `CuentaTest` | Unitario | Débito, crédito, saldo en cero, saldo insuficiente sin modificar la cuenta, importes inválidos (0, negativos, 3 decimales, nulo) |
| `RegistroTransaccionTest` | Unitario | Enmascarado de tarjetas de 16 y 19 dígitos |
| `CajeroServiceTest` | Unitario (Mockito) | Se audita lo aprobado (con saldos), lo rechazado y los errores inesperados; las excepciones se propagan |
| `CajeroControllerIntegrationTest` | Integración (MockMvc + H2) | Los 4 endpoints con casos felices y de error, códigos HTTP, validación de formato, JSON mal formado, 404/405, propagación de `X-Request-Id`, health check |
| `ConcurrenciaExtraccionTest` | Integración / concurrencia | 20 hilos simultáneos: no hay saldos negativos ni actualizaciones perdidas |
| `AuditoriaTransaccionalIntegrationTest` | Integración | El archivo `transacciones.log` contiene el registro JSON correcto, también para operaciones rechazadas, y nunca el PAN completo |

### atm-cli – `mvn test` → 42 tests, 0 fallos

```text
[INFO] Tests run: 10, Failures: 0, Errors: 0 - AtmCliTest
[INFO] Tests run: 3,  Failures: 0, Errors: 0 - FormatoMonedaTest
[INFO] Tests run: 7,  Failures: 0, Errors: 0 - HttpCajeroClientTest
[INFO] Tests run: 22, Failures: 0, Errors: 0 - ParserComandosTest
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Clase de test | Qué valida |
|---|---|
| `ParserComandosTest` | Parseo de las 4 operaciones, cantidad de parámetros, formato de tarjeta/CBU, importes válidos e inválidos |
| `FormatoMonedaTest` | Formato `$ 99999,99` |
| `AtmCliTest` | Mensajes exactos del enunciado, códigos de salida, que no se llame al servicio ante un uso incorrecto, mensaje de resultado incierto |
| `HttpCajeroClientTest` | Contra un servidor HTTP real embebido: request JSON y header `X-Request-Id`, mapeo de errores, respuestas no JSON (502), servicio caído, **timeout de lectura en extracción = resultado incierto**, timeout en consulta = no incierto |

## 2. Prueba de que el test de concurrencia detecta el problema

Se quitó temporalmente `@Lock(LockModeType.PESSIMISTIC_WRITE)` de `CuentaRepository` y se corrió `ConcurrenciaExtraccionTest` 3 veces:

```text
[ERROR] Tests run: 2, Failures: 2 - ConcurrenciaExtraccionTest
expected: 4500.00
 but was: 5050.00
expected: 10          <- se aprobaron mas de 10 extracciones de $100 sobre $1000
[ERROR] Tests run: 2, Failures: 2 - ConcurrenciaExtraccionTest
expected: 4500.00
 but was: 4900.00
...
```

Sin el bloqueo hay *lost updates*. Con el bloqueo restaurado los 2 tests pasan de forma consistente.

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
{"transaccionId":"4ae1b069-0e3b-4c1d-bdd7-85063357f820","cuenta":"1000000001","saldo":150000.00}
```

## 4. Log transaccional generado (`logs/transacciones.log`)

Extracto real de la corrida anterior:

```json
{"fechaHora":"2026-10-16T20:01:33.9937929-03:00","transaccionId":"93cf098d-87fa-4163-a5dd-a9cd96f2a53c","requestId":"ba0b72a9-566e-4df7-8fee-49efcde532b1","operacion":"LOGIN","resultado":"APROBADA","tarjeta":"451766******0001"}
{"fechaHora":"2026-10-16T20:01:34.4590264-03:00","transaccionId":"6f753529-96dc-4c4e-95f5-3b021356fa41","requestId":"9fb65fe5-1d8c-44f3-aae3-e27db65cc312","operacion":"LOGIN","resultado":"RECHAZADA","codigoError":"TARJETA_INACTIVA","tarjeta":"451766******0003"}
{"fechaHora":"2026-10-16T20:01:35.8550388-03:00","transaccionId":"8c4dcca7-06f9-40a8-a4c3-25cbcb434601","requestId":"c7505d18-dfb5-41ae-8a85-4f328815ef89","operacion":"EXTRACCION","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000002","importe":500.50,"saldoAnterior":2500.50,"saldoPosterior":2000.00}
{"fechaHora":"2026-10-16T20:01:36.724296-03:00","transaccionId":"251837ef-a6c2-4631-89ec-387f44021c35","requestId":"c8f0523a-7c5a-4b1c-bbb0-2e9d0c7acef2","operacion":"EXTRACCION","resultado":"RECHAZADA","codigoError":"SALDO_INSUFICIENTE","tarjeta":"451766******0001","cuenta":"1000000002","importe":999999}
{"fechaHora":"2026-10-16T20:01:38.0313134-03:00","transaccionId":"95efad66-b931-4efb-8dbb-0c1829388783","requestId":"503cd4f3-94ee-427a-bf75-65069ecb5ecd","operacion":"DEPOSITO","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000003","cbu":"0170001100000000000003","importe":1500.75,"saldoAnterior":10000.00,"saldoPosterior":11500.75}
{"fechaHora":"2026-10-16T20:01:38.9187931-03:00","transaccionId":"24822e10-1f8d-4723-81ab-b52cb901cd29","requestId":"3dea1203-4017-4a5e-83d0-3cca6086bd3f","operacion":"DEPOSITO","resultado":"RECHAZADA","codigoError":"CBU_INEXISTENTE","tarjeta":"451766******0001","cbu":"0170001199999999999999","importe":100}
{"fechaHora":"2026-10-16T20:02:14.2388166-03:00","transaccionId":"4ae1b069-0e3b-4c1d-bdd7-85063357f820","requestId":"demo-curl-1","operacion":"CONSULTA_SALDO","resultado":"APROBADA","tarjeta":"451766******0001","cuenta":"1000000001","saldoAnterior":150000.00,"saldoPosterior":150000.00}
```

Verificación de que el PAN completo no aparece en ningún log:

```text
$ grep -c "4517660000000001" logs/*.log
logs/atm-service.log:0
logs/transacciones.log:0
```

Con estos registros se puede reconstruir el saldo de cada cuenta: el `saldoPosterior` de un movimiento coincide con el `saldoAnterior` del siguiente movimiento de esa misma cuenta (2500,50 → 2000,00 en la cuenta `1000000002`).
