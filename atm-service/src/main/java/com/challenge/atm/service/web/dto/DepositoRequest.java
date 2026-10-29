package com.challenge.atm.service.web.dto;

import com.challenge.atm.service.constants.Constants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class DepositoRequest {

    @NotBlank
    @Pattern(regexp = Constants.REGEX_TARJETA, message = Constants.TARJETA_INVALIDA)
    private String tarjeta;

    @NotBlank
    @Pattern(regexp = Constants.REGEX_CBU, message = Constants.CBU_INVALIDO)
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
