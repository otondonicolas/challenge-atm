package com.challenge.atm.service.exception;

import org.springframework.http.HttpStatus;

public enum CodigoError {

    TARJETA_INEXISTENTE(HttpStatus.UNAUTHORIZED, "La tarjeta no existe"),
    TARJETA_INACTIVA(HttpStatus.FORBIDDEN, "La tarjeta se encuentra inactiva"),
    CUENTA_NO_ASOCIADA(HttpStatus.FORBIDDEN, "La cuenta no corresponde a la tarjeta"),
    CBU_INEXISTENTE(HttpStatus.NOT_FOUND, "No existe una cuenta con el CBU informado"),
    SALDO_INSUFICIENTE(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo insuficiente"),
    IMPORTE_INVALIDO(HttpStatus.BAD_REQUEST, "El importe debe ser mayor a cero y tener hasta dos decimales"),
    DATOS_INVALIDOS(HttpStatus.BAD_REQUEST, "Los datos de la solicitud son invalidos"),
    RECURSO_INEXISTENTE(HttpStatus.NOT_FOUND, "El recurso solicitado no existe"),
    ERROR_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo procesar la operacion. Intente nuevamente mas tarde");

    private final HttpStatus status;
    private final String mensaje;

    CodigoError(HttpStatus status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMensaje() {
        return mensaje;
    }
}
