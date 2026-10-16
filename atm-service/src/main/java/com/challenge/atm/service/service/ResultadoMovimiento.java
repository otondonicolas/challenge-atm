package com.challenge.atm.service.service;

import java.math.BigDecimal;

public final class ResultadoMovimiento {

    private final String cuenta;
    private final BigDecimal saldoAnterior;
    private final BigDecimal saldoPosterior;

    public ResultadoMovimiento(String cuenta, BigDecimal saldoAnterior, BigDecimal saldoPosterior) {
        this.cuenta = cuenta;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
    }

    public static ResultadoMovimiento consulta(String cuenta, BigDecimal saldo) {
        return new ResultadoMovimiento(cuenta, saldo, saldo);
    }

    public String getCuenta() {
        return cuenta;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }
}
