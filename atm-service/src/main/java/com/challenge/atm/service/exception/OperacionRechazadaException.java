package com.challenge.atm.service.exception;

public class OperacionRechazadaException extends RuntimeException {

    private final CodigoError codigo;

    public OperacionRechazadaException(CodigoError codigo) {
        super(codigo.getMensaje());
        this.codigo = codigo;
    }

    public CodigoError getCodigo() {
        return codigo;
    }
}
