package com.challenge.atm.service.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    private final String codigo;
    private final String mensaje;
    private final List<String> detalles;

    public ErrorResponse(String codigo, String mensaje, List<String> detalles) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.detalles = detalles == null ? Collections.<String>emptyList() : detalles;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<String> getDetalles() {
        return detalles;
    }
}
