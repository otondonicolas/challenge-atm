package com.challenge.atm.service.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class SaldoRequest {

    @NotBlank
    @Pattern(regexp = Validaciones.TARJETA, message = Validaciones.TARJETA_MENSAJE)
    private String tarjeta;

    @NotBlank
    @Pattern(regexp = Validaciones.CUENTA, message = Validaciones.CUENTA_MENSAJE)
    private String cuenta;

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }
}
