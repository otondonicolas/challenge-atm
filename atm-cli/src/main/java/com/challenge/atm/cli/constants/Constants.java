package com.challenge.atm.cli.constants;

public final class Constants {

    public static final String ARS = "$ ";
    public static final String FORMATO_IMPORTE = "0.00";

    public static final String INGRESO_EXITOSO = "Ingreso exitoso";
    public static final String INGRESO_NO_EXITOSO = "Ingreso no exitoso";
    public static final String RETIRE_SU_DINERO = "Retire su dinero";
    public static final String DEPOSITO_EXITOSO = "Depósito exitoso";
    public static final String SU_SALDO_ES = "Su saldo es %s";
    public static final String ERROR = "Error: %s";
    public static final String DETALLE = "  - %s";
    public static final String USO = "Uso:";
    public static final String LINEA_USO = "  atm %s";
    public static final String OPERACION_NO_SOPORTADA = "operacion no soportada";
    public static final String OPERACION_NO_SOPORTADA_TIPO = "operacion no soportada: %s";

    public static final String COMANDO_LOGIN = "login";
    public static final String COMANDO_EXTRAER = "extraer";
    public static final String COMANDO_DEPOSITAR = "depositar";
    public static final String COMANDO_SALDO = "saldo";
    public static final String SINTAXIS_LOGIN = "login <tarjeta>";
    public static final String SINTAXIS_EXTRAER = "extraer <tarjeta> <cuenta> <importe>";
    public static final String SINTAXIS_DEPOSITAR = "depositar <tarjeta> <cbu> <importe>";
    public static final String SINTAXIS_SALDO = "saldo <tarjeta> <cuenta>";

    public static final String REGEX_TARJETA = "\\d{16,19}";
    public static final String REGEX_CUENTA = "\\d{1,20}";
    public static final String REGEX_CBU = "\\d{22}";
    public static final String REGEX_IMPORTE = "\\d{1,13}([.,]\\d{1,2})?";

    public static final String DEBE_INDICAR_OPERACION = "debe indicar una operacion";
    public static final String OPERACION_DESCONOCIDA = "operacion desconocida '%s'";
    public static final String CANTIDAD_PARAMETROS_INCORRECTA = "cantidad de parametros incorrecta. Uso: atm %s";
    public static final String TARJETA_INVALIDA = "la tarjeta debe tener entre 16 y 19 digitos";
    public static final String CUENTA_INVALIDA = "la cuenta debe tener entre 1 y 20 digitos";
    public static final String CBU_INVALIDO = "el CBU debe tener 22 digitos";
    public static final String IMPORTE_INVALIDO = "importe invalido '%s'. Use el formato 1500 o 1500,50";
    public static final String IMPORTE_NO_POSITIVO = "el importe debe ser mayor a cero";

    public static final String URL_POR_DEFECTO = "http://localhost:8080";
    public static final String BARRA = "/";
    public static final String PROPIEDAD_URL = "atm.url";
    public static final String VARIABLE_URL = "ATM_SERVICE_URL";
    public static final String PROPIEDAD_CONNECT_TIMEOUT = "atm.connect-timeout-ms";
    public static final String VARIABLE_CONNECT_TIMEOUT = "ATM_CONNECT_TIMEOUT_MS";
    public static final String PROPIEDAD_READ_TIMEOUT = "atm.read-timeout-ms";
    public static final String VARIABLE_READ_TIMEOUT = "ATM_READ_TIMEOUT_MS";

    public static final String RUTA_BASE = "/api/v1/cajero";
    public static final String RUTA_LOGIN = "/login";
    public static final String RUTA_EXTRACCIONES = "/extracciones";
    public static final String RUTA_DEPOSITOS = "/depositos";
    public static final String RUTA_SALDO = "/saldo";
    public static final String METODO_POST = "POST";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    public static final String ACCEPT_JSON = "application/json";

    public static final String CAMPO_TARJETA = "tarjeta";
    public static final String CAMPO_CUENTA = "cuenta";
    public static final String CAMPO_CBU = "cbu";
    public static final String CAMPO_IMPORTE = "importe";
    public static final String CAMPO_SALDO = "saldo";
    public static final String CAMPO_CODIGO = "codigo";
    public static final String CAMPO_MENSAJE = "mensaje";
    public static final String CAMPO_DETALLES = "detalles";

    public static final String SERVICIO_NO_DISPONIBLE = "El servicio no se encuentra disponible. Intente nuevamente mas tarde";
    public static final String RESULTADO_INCIERTO =
            "No se pudo confirmar el resultado de la operacion. Consulte su saldo antes de reintentar (referencia %s)";
    public static final String RESPUESTA_INVALIDA = "El servicio devolvio una respuesta invalida";
    public static final String CODIGO_HTTP = "HTTP_%d";
    public static final String OPERACION_RECHAZADA_HTTP = "La operacion fue rechazada (HTTP %d)";

    private Constants() {
    }
}
