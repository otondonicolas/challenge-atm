package com.challenge.atm.service.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class ExtraccionRequest {

    @NotBlank
    @Pattern(regexp = Validaciones.TARJETA, message = Validaciones.TARJETA_MENSAJE)
    private String tarjeta;

    @NotBlank
    @Pattern(regexp = Validaciones.CUENTA, message = Validaciones.CUENTA_MENSAJE)
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
