package com.challenge.atm.service.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistroTransaccionTest {

    @Test
    void enmascaraTarjetaDejandoVisiblesBinYUltimosCuatroDigitos() {
        assertThat(RegistroTransaccion.Builder.enmascararTarjeta("4517660000000001")).isEqualTo("451766******0001");
    }

    @Test
    void enmascaraTarjetasDeDiecinueveDigitos() {
        assertThat(RegistroTransaccion.Builder.enmascararTarjeta("4517661234567890123")).isEqualTo("451766*********0123");
    }

    @Test
    void noEnmascaraValoresNulos() {
        assertThat(RegistroTransaccion.Builder.enmascararTarjeta(null)).isNull();
    }
}
