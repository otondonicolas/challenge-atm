package com.challenge.atm.service.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank
    @Pattern(regexp = Validaciones.TARJETA, message = Validaciones.TARJETA_MENSAJE)
    private String tarjeta;

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }
}
