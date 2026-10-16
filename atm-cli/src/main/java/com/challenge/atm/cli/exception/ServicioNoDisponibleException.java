package com.challenge.atm.cli.exception;

public class ServicioNoDisponibleException extends RuntimeException {

    private final boolean resultadoIncierto;
    private final String requestId;

    public ServicioNoDisponibleException(String mensaje, boolean resultadoIncierto, String requestId, Throwable causa) {
        super(mensaje, causa);
        this.resultadoIncierto = resultadoIncierto;
        this.requestId = requestId;
    }

    public boolean isResultadoIncierto() {
        return resultadoIncierto;
    }

    public String getRequestId() {
        return requestId;
    }
}
