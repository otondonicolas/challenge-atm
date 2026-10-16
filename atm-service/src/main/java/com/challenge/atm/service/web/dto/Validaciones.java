package com.challenge.atm.service.web.dto;

final class Validaciones {

    static final String TARJETA = "\\d{16,19}";
    static final String TARJETA_MENSAJE = "debe tener entre 16 y 19 digitos";
    static final String CUENTA = "\\d{1,20}";
    static final String CUENTA_MENSAJE = "debe tener entre 1 y 20 digitos";
    static final String CBU = "\\d{22}";
    static final String CBU_MENSAJE = "debe tener 22 digitos";

    private Validaciones() {
    }
}
