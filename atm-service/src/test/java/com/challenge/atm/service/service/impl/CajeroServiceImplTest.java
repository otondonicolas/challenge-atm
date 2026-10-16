package com.challenge.atm.service.service.impl;

import com.challenge.atm.service.audit.AuditoriaTransaccional;
import com.challenge.atm.service.audit.RegistroTransaccion;
import com.challenge.atm.service.audit.ResultadoTransaccion;
import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import com.challenge.atm.service.service.CajeroService;
import com.challenge.atm.service.service.MovimientoService;
import com.challenge.atm.service.service.ResultadoMovimiento;
import com.challenge.atm.service.service.ResultadoOperacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajeroServiceImplTest {

    private static final String TARJETA = "4517660000000001";
    private static final String CUENTA = "1000000001";

    @Mock
    private MovimientoService movimientoService;

    @Mock
    private AuditoriaTransaccional auditoria;

    private CajeroService cajeroService;

    @BeforeEach
    void setUp() {
        cajeroService = new CajeroServiceImpl(movimientoService, auditoria);
    }

    @Test
    void extraccionAprobadaSeAuditaConSaldosAnteriorYPosterior() {
        BigDecimal importe = new BigDecimal("100.00");
        when(movimientoService.extraer(TARJETA, CUENTA, importe))
                .thenReturn(new ResultadoMovimiento(CUENTA, new BigDecimal("500.00"), new BigDecimal("400.00")));

        ResultadoOperacion resultado = cajeroService.extraer(TARJETA, CUENTA, importe);

        RegistroTransaccion registro = registroAuditado();
        assertThat(registro.getResultado()).isEqualTo(ResultadoTransaccion.APROBADA);
        assertThat(registro.getTransaccionId()).isEqualTo(resultado.getTransaccionId());
        assertThat(registro.getTarjeta()).isEqualTo("451766******0001");
        assertThat(registro.getImporte()).isEqualByComparingTo("100.00");
        assertThat(registro.getSaldoAnterior()).isEqualByComparingTo("500.00");
        assertThat(registro.getSaldoPosterior()).isEqualByComparingTo("400.00");
        assertThat(resultado.getSaldo()).isEqualByComparingTo("400.00");
    }

    @Test
    void operacionRechazadaSeAuditaYSePropaga() {
        BigDecimal importe = new BigDecimal("999999.00");
        when(movimientoService.extraer(TARJETA, CUENTA, importe))
                .thenThrow(new OperacionRechazadaException(CodigoError.SALDO_INSUFICIENTE));

        assertThatThrownBy(() -> cajeroService.extraer(TARJETA, CUENTA, importe))
                .isInstanceOf(OperacionRechazadaException.class);

        RegistroTransaccion registro = registroAuditado();
        assertThat(registro.getResultado()).isEqualTo(ResultadoTransaccion.RECHAZADA);
        assertThat(registro.getCodigoError()).isEqualTo(CodigoError.SALDO_INSUFICIENTE);
        assertThat(registro.getSaldoPosterior()).isNull();
    }

    @Test
    void errorInesperadoSeAuditaComoErrorYSePropaga() {
        when(movimientoService.consultarSaldo(TARJETA, CUENTA)).thenThrow(new IllegalStateException("base no disponible"));

        assertThatThrownBy(() -> cajeroService.consultarSaldo(TARJETA, CUENTA))
                .isInstanceOf(IllegalStateException.class);

        RegistroTransaccion registro = registroAuditado();
        assertThat(registro.getResultado()).isEqualTo(ResultadoTransaccion.ERROR);
        assertThat(registro.getCodigoError()).isEqualTo(CodigoError.ERROR_INTERNO);
    }

    @Test
    void loginExitosoSeAudita() {
        cajeroService.login(TARJETA);

        verify(movimientoService).validarTarjetaActiva(TARJETA);
        assertThat(registroAuditado().getResultado()).isEqualTo(ResultadoTransaccion.APROBADA);
    }

    private RegistroTransaccion registroAuditado() {
        ArgumentCaptor<RegistroTransaccion.Builder> captor = ArgumentCaptor.forClass(RegistroTransaccion.Builder.class);
        verify(auditoria).registrar(captor.capture());
        return captor.getValue().construir("2026-01-01T00:00:00Z", null);
    }
}
