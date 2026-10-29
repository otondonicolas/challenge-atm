package com.challenge.atm.service.web.dto;

import com.challenge.atm.service.constants.Constants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class ExtraccionRequest {

    @NotBlank
    @Pattern(regexp = Constants.REGEX_TARJETA, message = Constants.TARJETA_INVALIDA)
    private String tarjeta;

    @NotBlank
    @Pattern(regexp = Constants.REGEX_CUENTA, message = Constants.CUENTA_INVALIDA)
    private String cuenta;

    @NotNull
    private BigDecimal importe;

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

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}
