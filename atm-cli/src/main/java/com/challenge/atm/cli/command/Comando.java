package com.challenge.atm.cli.command;

import java.math.BigDecimal;

public final class Comando {

    private final TipoComando tipo;
    private final String tarjeta;
    private final String cuenta;
    private final String cbu;
    private final BigDecimal importe;

    private Comando(TipoComando tipo, String tarjeta, String cuenta, String cbu, BigDecimal importe) {
        this.tipo = tipo;
        this.tarjeta = tarjeta;
        this.cuenta = cuenta;
        this.cbu = cbu;
        this.importe = importe;
    }

    public static Comando login(String tarjeta) {
        return new Comando(TipoComando.LOGIN, tarjeta, null, null, null);
    }

    public static Comando extraer(String tarjeta, String cuenta, BigDecimal importe) {
        return new Comando(TipoComando.EXTRAER, tarjeta, cuenta, null, importe);
    }

    public static Comando depositar(String tarjeta, String cbu, BigDecimal importe) {
        return new Comando(TipoComando.DEPOSITAR, tarjeta, null, cbu, importe);
    }

    public static Comando saldo(String tarjeta, String cuenta) {
        return new Comando(TipoComando.SALDO, tarjeta, cuenta, null, null);
    }

    public TipoComando getTipo() {
        return tipo;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public String getCuenta() {
        return cuenta;
    }

    public String getCbu() {
        return cbu;
    }

    public BigDecimal getImporte() {
        return importe;
    }
}
