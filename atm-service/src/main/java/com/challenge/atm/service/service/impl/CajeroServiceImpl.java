package com.challenge.atm.service.service.impl;

import com.challenge.atm.service.audit.AuditoriaTransaccional;
import com.challenge.atm.service.audit.RegistroTransaccion;
import com.challenge.atm.service.audit.TipoOperacion;
import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import com.challenge.atm.service.service.CajeroService;
import com.challenge.atm.service.service.MovimientoService;
import com.challenge.atm.service.service.ResultadoMovimiento;
import com.challenge.atm.service.service.ResultadoOperacion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class CajeroServiceImpl implements CajeroService {

    private final MovimientoService movimientoService;
    private final AuditoriaTransaccional auditoria;

    public CajeroServiceImpl(MovimientoService movimientoService, AuditoriaTransaccional auditoria) {
        this.movimientoService = movimientoService;
        this.auditoria = auditoria;
    }

    @Override
    public ResultadoOperacion login(String tarjeta) {
        return ejecutar(RegistroTransaccion.nuevo(nuevoId(), TipoOperacion.LOGIN).tarjeta(tarjeta),
                () -> {
                    movimientoService.validarTarjetaActiva(tarjeta);
                    return null;
                });
    }

    @Override
    public ResultadoOperacion extraer(String tarjeta, String cuenta, BigDecimal importe) {
        return ejecutar(RegistroTransaccion.nuevo(nuevoId(), TipoOperacion.EXTRACCION)
                        .tarjeta(tarjeta).cuenta(cuenta).importe(importe),
                () -> movimientoService.extraer(tarjeta, cuenta, importe));
    }

    @Override
    public ResultadoOperacion depositar(String tarjeta, String cbu, BigDecimal importe) {
        return ejecutar(RegistroTransaccion.nuevo(nuevoId(), TipoOperacion.DEPOSITO)
                        .tarjeta(tarjeta).cbu(cbu).importe(importe),
                () -> movimientoService.depositar(tarjeta, cbu, importe));
    }

    @Override
    public ResultadoOperacion consultarSaldo(String tarjeta, String cuenta) {
        return ejecutar(RegistroTransaccion.nuevo(nuevoId(), TipoOperacion.CONSULTA_SALDO)
                        .tarjeta(tarjeta).cuenta(cuenta),
                () -> movimientoService.consultarSaldo(tarjeta, cuenta));
    }

    private ResultadoOperacion ejecutar(RegistroTransaccion.Builder registro, Supplier<ResultadoMovimiento> operacion) {
        try {
            ResultadoMovimiento movimiento = operacion.get();
            auditoria.registrar(registro.aprobada(movimiento));
            return new ResultadoOperacion(registro.getTransaccionId(), movimiento);
        } catch (OperacionRechazadaException e) {
            auditoria.registrar(registro.rechazada(e.getCodigo()));
            throw e;
        } catch (RuntimeException e) {
            auditoria.registrar(registro.fallida(CodigoError.ERROR_INTERNO));
            throw e;
        }
    }

    private static String nuevoId() {
        return UUID.randomUUID().toString();
    }
}
