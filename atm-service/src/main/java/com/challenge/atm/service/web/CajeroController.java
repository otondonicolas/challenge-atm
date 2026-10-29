package com.challenge.atm.service.web;

import com.challenge.atm.service.constants.Constants;
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
@RequestMapping(Constants.RUTA_BASE)
public class CajeroController {

    private final CajeroService cajeroService;

    public CajeroController(CajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @PostMapping(Constants.RUTA_LOGIN)
    public OperacionResponse login(@Valid @RequestBody LoginRequest request) {
        ResultadoOperacion resultado = cajeroService.login(request.getTarjeta());
        return new OperacionResponse(resultado.getTransaccionId(), Constants.INGRESO_EXITOSO);
    }

    @PostMapping(Constants.RUTA_EXTRACCIONES)
    public OperacionResponse extraer(@Valid @RequestBody ExtraccionRequest request) {
        ResultadoOperacion resultado = cajeroService.extraer(request.getTarjeta(), request.getCuenta(), request.getImporte());
        return new OperacionResponse(resultado.getTransaccionId(), Constants.RETIRE_SU_DINERO);
    }

    @PostMapping(Constants.RUTA_DEPOSITOS)
    public OperacionResponse depositar(@Valid @RequestBody DepositoRequest request) {
        ResultadoOperacion resultado = cajeroService.depositar(request.getTarjeta(), request.getCbu(), request.getImporte());
        return new OperacionResponse(resultado.getTransaccionId(), Constants.DEPOSITO_EXITOSO);
    }

    @PostMapping(Constants.RUTA_SALDO)
    public SaldoResponse consultarSaldo(@Valid @RequestBody SaldoRequest request) {
        ResultadoOperacion resultado = cajeroService.consultarSaldo(request.getTarjeta(), request.getCuenta());
        return new SaldoResponse(resultado.getTransaccionId(), resultado.getCuenta(), resultado.getSaldo());
    }
}
