package com.challenge.atm.cli;

import java.math.BigDecimal;

public interface CajeroClient {

    void login(String tarjeta);

    void extraer(String tarjeta, String cuenta, BigDecimal importe);

    void depositar(String tarjeta, String cbu, BigDecimal importe);

    BigDecimal consultarSaldo(String tarjeta, String cuenta);
}
