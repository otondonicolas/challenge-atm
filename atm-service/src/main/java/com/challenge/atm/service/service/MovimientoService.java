package com.challenge.atm.service.service;

import com.challenge.atm.service.domain.Cuenta;
import com.challenge.atm.service.domain.Tarjeta;
import com.challenge.atm.service.domain.TarjetaCuentaId;
import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import com.challenge.atm.service.repository.CuentaRepository;
import com.challenge.atm.service.repository.TarjetaCuentaRepository;
import com.challenge.atm.service.repository.TarjetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MovimientoService {

    private final TarjetaRepository tarjetaRepository;
    private final CuentaRepository cuentaRepository;
    private final TarjetaCuentaRepository tarjetaCuentaRepository;

    public MovimientoService(TarjetaRepository tarjetaRepository,
                             CuentaRepository cuentaRepository,
                             TarjetaCuentaRepository tarjetaCuentaRepository) {
        this.tarjetaRepository = tarjetaRepository;
        this.cuentaRepository = cuentaRepository;
        this.tarjetaCuentaRepository = tarjetaCuentaRepository;
    }

    @Transactional(readOnly = true)
    public void validarTarjetaActiva(String numeroTarjeta) {
        verificarTarjetaActiva(numeroTarjeta);
    }

    @Transactional
    public ResultadoMovimiento extraer(String numeroTarjeta, String numeroCuenta, BigDecimal importe) {
        verificarTarjetaActiva(numeroTarjeta);
        verificarCuentaAsociada(numeroTarjeta, numeroCuenta);
        Cuenta cuenta = cuentaRepository.findByNumeroParaActualizar(numeroCuenta)
                .orElseThrow(() -> new OperacionRechazadaException(CodigoError.CUENTA_NO_ASOCIADA));
        BigDecimal saldoAnterior = cuenta.getSaldo();
        cuenta.debitar(importe);
        return new ResultadoMovimiento(cuenta.getNumero(), saldoAnterior, cuenta.getSaldo());
    }

    @Transactional
    public ResultadoMovimiento depositar(String numeroTarjeta, String cbu, BigDecimal importe) {
        verificarTarjetaActiva(numeroTarjeta);
        Cuenta cuenta = cuentaRepository.findByCbuParaActualizar(cbu)
                .orElseThrow(() -> new OperacionRechazadaException(CodigoError.CBU_INEXISTENTE));
        BigDecimal saldoAnterior = cuenta.getSaldo();
        cuenta.acreditar(importe);
        return new ResultadoMovimiento(cuenta.getNumero(), saldoAnterior, cuenta.getSaldo());
    }

    @Transactional(readOnly = true)
    public ResultadoMovimiento consultarSaldo(String numeroTarjeta, String numeroCuenta) {
        verificarTarjetaActiva(numeroTarjeta);
        verificarCuentaAsociada(numeroTarjeta, numeroCuenta);
        Cuenta cuenta = cuentaRepository.findById(numeroCuenta)
                .orElseThrow(() -> new OperacionRechazadaException(CodigoError.CUENTA_NO_ASOCIADA));
        return ResultadoMovimiento.consulta(cuenta.getNumero(), cuenta.getSaldo());
    }

    private void verificarTarjetaActiva(String numeroTarjeta) {
        Tarjeta tarjeta = tarjetaRepository.findById(numeroTarjeta)
                .orElseThrow(() -> new OperacionRechazadaException(CodigoError.TARJETA_INEXISTENTE));
        if (!tarjeta.estaActiva()) {
            throw new OperacionRechazadaException(CodigoError.TARJETA_INACTIVA);
        }
    }

    private void verificarCuentaAsociada(String numeroTarjeta, String numeroCuenta) {
        if (!tarjetaCuentaRepository.existsById(new TarjetaCuentaId(numeroTarjeta, numeroCuenta))) {
            throw new OperacionRechazadaException(CodigoError.CUENTA_NO_ASOCIADA);
        }
    }
}
