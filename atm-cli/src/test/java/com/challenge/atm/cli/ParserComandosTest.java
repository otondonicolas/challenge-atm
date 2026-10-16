package com.challenge.atm.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParserComandosTest {

    private static final String TARJETA = "4517660000000001";

    @Test
    void parseaLogin() {
        Comando comando = ParserComandos.parsear(new String[]{"login", TARJETA});

        assertThat(comando.getTipo()).isEqualTo(TipoComando.LOGIN);
        assertThat(comando.getTarjeta()).isEqualTo(TARJETA);
    }

    @Test
    void parseaExtraccion() {
        Comando comando = ParserComandos.parsear(new String[]{"extraer", TARJETA, "1000000001", "1500,50"});

        assertThat(comando.getTipo()).isEqualTo(TipoComando.EXTRAER);
        assertThat(comando.getCuenta()).isEqualTo("1000000001");
        assertThat(comando.getImporte()).isEqualByComparingTo("1500.50");
    }

    @Test
    void parseaDeposito() {
        Comando comando = ParserComandos.parsear(new String[]{"depositar", TARJETA, "0170001100000000000003", "200"});

        assertThat(comando.getTipo()).isEqualTo(TipoComando.DEPOSITAR);
        assertThat(comando.getCbu()).isEqualTo("0170001100000000000003");
        assertThat(comando.getImporte()).isEqualByComparingTo("200");
    }

    @Test
    void parseaSaldo() {
        Comando comando = ParserComandos.parsear(new String[]{"SALDO", TARJETA, "1000000001"});

        assertThat(comando.getTipo()).isEqualTo(TipoComando.SALDO);
        assertThat(comando.getCuenta()).isEqualTo("1000000001");
    }

    @Test
    void sinArgumentosEsInvalido() {
        assertThatThrownBy(() -> ParserComandos.parsear(new String[0]))
                .isInstanceOf(ArgumentosInvalidosException.class);
    }

    @Test
    void operacionDesconocidaEsInvalida() {
        assertThatThrownBy(() -> ParserComandos.parsear(new String[]{"transferir", TARJETA}))
                .hasMessageContaining("operacion desconocida");
    }

    @Test
    void cantidadDeParametrosIncorrectaMuestraLaSintaxis() {
        assertThatThrownBy(() -> ParserComandos.parsear(new String[]{"extraer", TARJETA, "1000000001"}))
                .hasMessageContaining("extraer <tarjeta> <cuenta> <importe>");
    }

    @Test
    void tarjetaConFormatoInvalidoEsRechazada() {
        assertThatThrownBy(() -> ParserComandos.parsear(new String[]{"login", "1234"}))
                .hasMessageContaining("tarjeta");
    }

    @Test
    void cbuConFormatoInvalidoEsRechazado() {
        assertThatThrownBy(() -> ParserComandos.parsear(new String[]{"depositar", TARJETA, "123", "10"}))
                .hasMessageContaining("CBU");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0,00", "-10", "10.123", "abc", "1.000,50", ""})
    void importesInvalidosSonRechazados(String importe) {
        assertThatThrownBy(() -> ParserComandos.parsearImporte(importe))
                .isInstanceOf(ArgumentosInvalidosException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "10.5", "10,5", "10.50", "10,50", "0.01"})
    void importesValidosSonAceptados(String importe) {
        assertThat(ParserComandos.parsearImporte(importe).signum()).isPositive();
    }
}
