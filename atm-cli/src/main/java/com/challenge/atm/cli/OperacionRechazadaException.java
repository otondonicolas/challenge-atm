package com.challenge.atm.cli;

import java.util.Collections;
import java.util.List;

public class OperacionRechazadaException extends RuntimeException {

    private final int status;
    private final String codigo;
    private final List<String> detalles;

    public OperacionRechazadaException(int status, String codigo, String mensaje, List<String> detalles) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
        this.detalles = detalles == null ? Collections.<String>emptyList() : detalles;
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<String> getDetalles() {
        return detalles;
    }
}
