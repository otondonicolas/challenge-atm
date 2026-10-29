package com.challenge.atm.service.constants;

public final class Constants {

    public static final String RUTA_BASE = "/api/v1/cajero";
    public static final String RUTA_LOGIN = "/login";
    public static final String RUTA_EXTRACCIONES = "/extracciones";
    public static final String RUTA_DEPOSITOS = "/depositos";
    public static final String RUTA_SALDO = "/saldo";

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String REGEX_REQUEST_ID = "[A-Za-z0-9-]{1,64}";

    public static final String INGRESO_EXITOSO = "Ingreso exitoso";
    public static final String RETIRE_SU_DINERO = "Retire su dinero";
    public static final String DEPOSITO_EXITOSO = "Depósito exitoso";
    public static final String METODO_NO_SOPORTADO = "Metodo HTTP no soportado";

    public static final String MENSAJE_TARJETA_INEXISTENTE = "La tarjeta no existe";
    public static final String MENSAJE_TARJETA_INACTIVA = "La tarjeta se encuentra inactiva";
    public static final String MENSAJE_CUENTA_NO_ASOCIADA = "La cuenta no corresponde a la tarjeta";
    public static final String MENSAJE_CBU_INEXISTENTE = "No existe una cuenta con el CBU informado";
    public static final String MENSAJE_SALDO_INSUFICIENTE = "Saldo insuficiente";
    public static final String MENSAJE_IMPORTE_INVALIDO = "El importe debe ser mayor a cero y tener hasta dos decimales";
    public static final String MENSAJE_DATOS_INVALIDOS = "Los datos de la solicitud son invalidos";
    public static final String MENSAJE_RECURSO_INEXISTENTE = "El recurso solicitado no existe";
    public static final String MENSAJE_ERROR_INTERNO = "No se pudo procesar la operacion. Intente nuevamente mas tarde";

    public static final String REGEX_TARJETA = "\\d{16,19}";
    public static final String REGEX_CUENTA = "\\d{1,20}";
    public static final String REGEX_CBU = "\\d{22}";
    public static final String TARJETA_INVALIDA = "debe tener entre 16 y 19 digitos";
    public static final String CUENTA_INVALIDA = "debe tener entre 1 y 20 digitos";
    public static final String CBU_INVALIDO = "debe tener 22 digitos";
    public static final String DETALLE_CAMPO = "%s: %s";

    public static final String OPERACION_RECHAZADA = "Operacion rechazada: {}";
    public static final String SOLICITUD_INVALIDA = "Solicitud invalida: {}";
    public static final String CUERPO_ILEGIBLE = "Cuerpo de solicitud ilegible: {}";
    public static final String ERROR_INESPERADO = "Error inesperado procesando la solicitud";
    public static final String ERROR_SERIALIZANDO_REGISTRO = "No se pudo serializar el registro transaccional {}";

    public static final String LOGGER_AUDITORIA = "AUDITORIA_TRANSACCIONAL";

    private Constants() {
    }
}
