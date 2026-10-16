package com.challenge.atm.service.web.dto;

public class OperacionResponse {

    private final String transaccionId;
    private final String mensaje;

    public OperacionResponse(String transaccionId, String mensaje) {
        this.transaccionId = transaccionId;
        this.mensaje = mensaje;
    }

    public String getTransaccionId() {
        return transaccionId;
    }

    public String getMensaje() {
        return mensaje;
    }
}
