package com.challenge.atm.service.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class DepositoRequest {

    @NotBlank
    @Pattern(regexp = Validaciones.TARJETA, message = Validaciones.TARJETA_MENSAJE)
    private String tarjeta;

    @NotBlank
    @Pattern(regexp = Validaciones.CBU, message = Validaciones.CBU_MENSAJE)
    private String cbu;

    @NotNull
    private BigDecimal importe;

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}
