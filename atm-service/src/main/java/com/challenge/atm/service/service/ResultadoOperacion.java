package com.challenge.atm.service.service;

import java.math.BigDecimal;

public final class ResultadoOperacion {

    private final String transaccionId;
    private final ResultadoMovimiento movimiento;

    public ResultadoOperacion(String transaccionId, ResultadoMovimiento movimiento) {
        this.transaccionId = transaccionId;
        this.movimiento = movimiento;
    }

    public String getTransaccionId() {
        return transaccionId;
    }

    public String getCuenta() {
        return movimiento == null ? null : movimiento.getCuenta();
    }

    public BigDecimal getSaldo() {
        return movimiento == null ? null : movimiento.getSaldoPosterior();
    }
}
