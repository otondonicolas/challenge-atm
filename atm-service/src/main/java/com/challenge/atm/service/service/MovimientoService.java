package com.challenge.atm.service.service;

import java.math.BigDecimal;

public interface MovimientoService {

    void validarTarjetaActiva(String numeroTarjeta);

    ResultadoMovimiento extraer(String numeroTarjeta, String numeroCuenta, BigDecimal importe);

    ResultadoMovimiento depositar(String numeroTarjeta, String cbu, BigDecimal importe);

    ResultadoMovimiento consultarSaldo(String numeroTarjeta, String numeroCuenta);
}
