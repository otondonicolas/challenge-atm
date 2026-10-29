package com.challenge.atm.service.web.dto;

import com.challenge.atm.service.constants.Constants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank
    @Pattern(regexp = Constants.REGEX_TARJETA, message = Constants.TARJETA_INVALIDA)
    private String tarjeta;

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }
}
