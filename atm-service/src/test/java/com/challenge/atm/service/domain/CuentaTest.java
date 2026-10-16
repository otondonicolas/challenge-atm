package com.challenge.atm.service.domain;

import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private Cuenta cuentaConSaldo(String saldo) {
        return new Cuenta("1", "0000000000000000000001", new BigDecimal(saldo));
    }

    @Test
    void debitarDescuentaElImporteDelSaldo() {
        Cuenta cuenta = cuentaConSaldo("1000.00");

        cuenta.debitar(new BigDecimal("250.50"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("749.50");
    }

    @Test
    void debitarPermiteDejarLaCuentaEnCero() {
        Cuenta cuenta = cuentaConSaldo("100.00");

        cuenta.debitar(new BigDecimal("100"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void debitarMasQueElSaldoSeRechazaSinModificarLaCuenta() {
        Cuenta cuenta = cuentaConSaldo("100.00");

        assertThatThrownBy(() -> cuenta.debitar(new BigDecimal("100.01")))
                .isInstanceOf(OperacionRechazadaException.class)
                .extracting("codigo").isEqualTo(CodigoError.SALDO_INSUFICIENTE);
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void acreditarSumaElImporteAlSaldo() {
        Cuenta cuenta = cuentaConSaldo("10.00");

        cuenta.acreditar(new BigDecimal("0.01"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("10.01");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01", "10.001"})
    void importesInvalidosSeRechazan(String importe) {
        Cuenta cuenta = cuentaConSaldo("1000.00");

        assertThatThrownBy(() -> cuenta.debitar(new BigDecimal(importe)))
                .extracting("codigo").isEqualTo(CodigoError.IMPORTE_INVALIDO);
        assertThatThrownBy(() -> cuenta.acreditar(new BigDecimal(importe)))
                .extracting("codigo").isEqualTo(CodigoError.IMPORTE_INVALIDO);
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void importeNuloSeRechaza() {
        Cuenta cuenta = cuentaConSaldo("1000.00");

        assertThatThrownBy(() -> cuenta.debitar(null))
                .extracting("codigo").isEqualTo(CodigoError.IMPORTE_INVALIDO);
    }

    @Test
    void importeConCerosDecimalesSobrantesEsValido() {
        Cuenta cuenta = cuentaConSaldo("1000.00");

        cuenta.debitar(new BigDecimal("10.5000"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("989.50");
    }
}
