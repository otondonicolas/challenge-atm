package com.challenge.atm.service.web;

import com.challenge.atm.service.service.CajeroService;
import com.challenge.atm.service.service.ResultadoOperacion;
import com.challenge.atm.service.web.dto.DepositoRequest;
import com.challenge.atm.service.web.dto.ExtraccionRequest;
import com.challenge.atm.service.web.dto.LoginRequest;
import com.challenge.atm.service.web.dto.OperacionResponse;
import com.challenge.atm.service.web.dto.SaldoRequest;
import com.challenge.atm.service.web.dto.SaldoResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/cajero")
public class CajeroController {

    private final CajeroService cajeroService;

    public CajeroController(CajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @PostMapping("/login")
    public OperacionResponse login(@Valid @RequestBody LoginRequest request) {
        ResultadoOperacion resultado = cajeroService.login(request.getTarjeta());
        return new OperacionResponse(resultado.getTransaccionId(), "Ingreso exitoso");
    }

    @PostMapping("/extracciones")
    public OperacionResponse extraer(@Valid @RequestBody ExtraccionRequest request) {
        ResultadoOperacion resultado = cajeroService.extraer(request.getTarjeta(), request.getCuenta(), request.getImporte());
        return new OperacionResponse(resultado.getTransaccionId(), "Retire su dinero");
    }

    @PostMapping("/depositos")
    public OperacionResponse depositar(@Valid @RequestBody DepositoRequest request) {
        ResultadoOperacion resultado = cajeroService.depositar(request.getTarjeta(), request.getCbu(), request.getImporte());
        return new OperacionResponse(resultado.getTransaccionId(), "Depósito exitoso");
    }

    @PostMapping("/saldo")
    public SaldoResponse consultarSaldo(@Valid @RequestBody SaldoRequest request) {
        ResultadoOperacion resultado = cajeroService.consultarSaldo(request.getTarjeta(), request.getCuenta());
        return new SaldoResponse(resultado.getTransaccionId(), resultado.getCuenta(), resultado.getSaldo());
    }
}
