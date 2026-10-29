#!/usr/bin/env sh

BASE=http://localhost:8080/api/v1/cajero

echo "== 0. Health check (esperado 200, UP)"
curl -s http://localhost:8080/actuator/health
echo

echo "== 1.1 Login OK (200, Ingreso exitoso)"
curl -s -i -X POST "$BASE/login" -H "Content-Type: application/json" -H "X-Request-Id: demo-login-ok" -d '{"tarjeta":"4517660000000001"}'
echo

echo "== 1.2 Login tarjeta inactiva (403, TARJETA_INACTIVA)"
curl -s -X POST "$BASE/login" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000003"}'
echo

echo "== 1.3 Login tarjeta inexistente (401, TARJETA_INEXISTENTE)"
curl -s -X POST "$BASE/login" -H "Content-Type: application/json" -d '{"tarjeta":"9999999999999999"}'
echo

echo "== 2.1 Saldo OK (200)"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000002"}'
echo

echo "== 2.2 Saldo cuenta de otra tarjeta (403, CUENTA_NO_ASOCIADA)"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000003"}'
echo

echo "== 2.3 Saldo tarjeta inactiva (403, TARJETA_INACTIVA)"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000003","cuenta":"1000000004"}'
echo

echo "== 3.1 Saldo antes de extraer"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000001"}'
echo

echo "== 3.2 Extraccion OK de 500,50 (200, Retire su dinero)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000001","importe":500.50}'
echo

echo "== 3.3 Saldo despues de extraer (500,50 menos)"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000001"}'
echo

echo "== 3.4 Extraccion saldo insuficiente (422, SALDO_INSUFICIENTE)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000002","importe":99999999}'
echo

echo "== 3.5 Extraccion cuenta de otra tarjeta (403, CUENTA_NO_ASOCIADA)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000003","importe":10}'
echo

echo "== 3.6 Extraccion tarjeta inactiva (403, TARJETA_INACTIVA)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000003","cuenta":"1000000004","importe":10}'
echo

echo "== 3.7 Extraccion importe cero (400, IMPORTE_INVALIDO)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000001","importe":0}'
echo

echo "== 3.8 Extraccion con mas de 2 decimales (400, IMPORTE_INVALIDO)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cuenta":"1000000001","importe":10.123}'
echo

echo "== 4.1 Saldo del destinatario antes (tarjeta 0002, cuenta 1000000003)"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000002","cuenta":"1000000003"}'
echo

echo "== 4.2 Deposito OK a cuenta de un tercero (200, Deposito exitoso)"
curl -s -X POST "$BASE/depositos" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cbu":"0170001100000000000003","importe":1500}'
echo

echo "== 4.3 Saldo del destinatario despues (1500 mas)"
curl -s -X POST "$BASE/saldo" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000002","cuenta":"1000000003"}'
echo

echo "== 4.4 Deposito CBU inexistente (404, CBU_INEXISTENTE)"
curl -s -X POST "$BASE/depositos" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cbu":"0170001100000000009999","importe":10}'
echo

echo "== 4.5 Deposito tarjeta inactiva (403, TARJETA_INACTIVA)"
curl -s -X POST "$BASE/depositos" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000003","cbu":"0170001100000000000003","importe":10}'
echo

echo "== 4.6 Deposito importe negativo (400, IMPORTE_INVALIDO)"
curl -s -X POST "$BASE/depositos" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cbu":"0170001100000000000003","importe":-5}'
echo

echo "== 5.1 Tarjeta con formato invalido (400, DATOS_INVALIDOS con detalles)"
curl -s -X POST "$BASE/login" -H "Content-Type: application/json" -d '{"tarjeta":"45176600ABC"}'
echo

echo "== 5.2 CBU con formato invalido (400, DATOS_INVALIDOS)"
curl -s -X POST "$BASE/depositos" -H "Content-Type: application/json" -d '{"tarjeta":"4517660000000001","cbu":"123","importe":10}'
echo

echo "== 5.3 Body vacio (400, DATOS_INVALIDOS)"
curl -s -X POST "$BASE/extracciones" -H "Content-Type: application/json" -d '{}'
echo

echo "== 5.4 JSON mal formado (400, DATOS_INVALIDOS)"
curl -s -X POST "$BASE/login" -H "Content-Type: application/json" -d '{"tarjeta": '
echo

echo "== 5.5 Endpoint inexistente (404, RECURSO_INEXISTENTE)"
curl -s -X POST "$BASE/transferencias" -H "Content-Type: application/json" -d '{}'
echo

echo "== 5.6 Metodo GET no permitido (405)"
curl -s -i "$BASE/login"
echo
