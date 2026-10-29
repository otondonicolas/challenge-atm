package com.challenge.atm.service.exception;

import com.challenge.atm.service.constants.Constants;
import org.springframework.http.HttpStatus;

public enum CodigoError {

    TARJETA_INEXISTENTE(HttpStatus.UNAUTHORIZED, Constants.MENSAJE_TARJETA_INEXISTENTE),
    TARJETA_INACTIVA(HttpStatus.FORBIDDEN, Constants.MENSAJE_TARJETA_INACTIVA),
    CUENTA_NO_ASOCIADA(HttpStatus.FORBIDDEN, Constants.MENSAJE_CUENTA_NO_ASOCIADA),
    CBU_INEXISTENTE(HttpStatus.NOT_FOUND, Constants.MENSAJE_CBU_INEXISTENTE),
    SALDO_INSUFICIENTE(HttpStatus.UNPROCESSABLE_ENTITY, Constants.MENSAJE_SALDO_INSUFICIENTE),
    IMPORTE_INVALIDO(HttpStatus.BAD_REQUEST, Constants.MENSAJE_IMPORTE_INVALIDO),
    DATOS_INVALIDOS(HttpStatus.BAD_REQUEST, Constants.MENSAJE_DATOS_INVALIDOS),
    RECURSO_INEXISTENTE(HttpStatus.NOT_FOUND, Constants.MENSAJE_RECURSO_INEXISTENTE),
    ERROR_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, Constants.MENSAJE_ERROR_INTERNO);

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
