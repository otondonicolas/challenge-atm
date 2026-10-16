package com.challenge.atm.cli.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FormatoMonedaTest {

    @Test
    void usaComaDecimalYDosDecimalesSinSeparadorDeMiles() {
        assertThat(FormatoMoneda.formatear(new BigDecimal("99999.99"))).isEqualTo("$ 99999,99");
    }

    @Test
    void completaLosDecimales() {
        assertThat(FormatoMoneda.formatear(new BigDecimal("150000"))).isEqualTo("$ 150000,00");
    }

    @Test
    void formateaSaldoCero() {
        assertThat(FormatoMoneda.formatear(BigDecimal.ZERO)).isEqualTo("$ 0,00");
    }
}
