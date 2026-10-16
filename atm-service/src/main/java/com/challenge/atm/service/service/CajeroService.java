package com.challenge.atm.service.service;

import java.math.BigDecimal;

public interface CajeroService {

    ResultadoOperacion login(String tarjeta);

    ResultadoOperacion extraer(String tarjeta, String cuenta, BigDecimal importe);

    ResultadoOperacion depositar(String tarjeta, String cbu, BigDecimal importe);

    ResultadoOperacion consultarSaldo(String tarjeta, String cuenta);
}
