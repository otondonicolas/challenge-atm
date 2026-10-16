package com.challenge.atm.service.web.dto;

import java.math.BigDecimal;

public class SaldoResponse {

    private final String transaccionId;
    private final String cuenta;
    private final BigDecimal saldo;

    public SaldoResponse(String transaccionId, String cuenta, BigDecimal saldo) {
        this.transaccionId = transaccionId;
        this.cuenta = cuenta;
        this.saldo = saldo;
    }

    public String getTransaccionId() {
        return transaccionId;
    }

    public String getCuenta() {
        return cuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
