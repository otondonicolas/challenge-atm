package com.challenge.atm.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class AtmCliTest {

    private static final String TARJETA = "4517660000000001";

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ClienteFalso cliente = new ClienteFalso();
    private final AtmCli cli;

    AtmCliTest() throws UnsupportedEncodingException {
        cli = new AtmCli(cliente, new PrintStream(buffer, true, "UTF-8"));
    }

    private String salida() throws UnsupportedEncodingException {
        return buffer.toString("UTF-8").trim();
    }

    @Test
    void loginExitoso() throws Exception {
        int codigo = cli.ejecutar(new String[]{"login", TARJETA});

        assertThat(codigo).isEqualTo(AtmCli.EXITO);
        assertThat(salida()).isEqualTo("Ingreso exitoso");
    }

    @Test
    void loginRechazadoMuestraIngresoNoExitoso() throws Exception {
        cliente.error = new OperacionRechazadaException(403, "TARJETA_INACTIVA", "La tarjeta se encuentra inactiva", null);

        int codigo = cli.ejecutar(new String[]{"login", TARJETA});

        assertThat(codigo).isEqualTo(AtmCli.OPERACION_RECHAZADA);
        assertThat(salida()).isEqualTo("Ingreso no exitoso");
    }

    @Test
    void loginConServicioCaidoMuestraIngresoNoExitosoYElMotivo() throws Exception {
        cliente.error = new ServicioNoDisponibleException("El servicio no se encuentra disponible", false, "r1", null);

        int codigo = cli.ejecutar(new String[]{"login", TARJETA});

        assertThat(codigo).isEqualTo(AtmCli.SERVICIO_NO_DISPONIBLE);
        assertThat(salida()).startsWith("Ingreso no exitoso").contains("El servicio no se encuentra disponible");
    }

    @Test
    void extraccionExitosa() throws Exception {
        int codigo = cli.ejecutar(new String[]{"extraer", TARJETA, "1000000001", "100"});

        assertThat(codigo).isEqualTo(AtmCli.EXITO);
        assertThat(salida()).isEqualTo("Retire su dinero");
        assertThat(cliente.ultimoImporte).isEqualByComparingTo("100");
    }

    @Test
    void extraccionRechazadaMuestraElMotivo() throws Exception {
        cliente.error = new OperacionRechazadaException(422, "SALDO_INSUFICIENTE", "Saldo insuficiente", null);

        int codigo = cli.ejecutar(new String[]{"extraer", TARJETA, "1000000001", "100"});

        assertThat(codigo).isEqualTo(AtmCli.OPERACION_RECHAZADA);
        assertThat(salida()).isEqualTo("Error: Saldo insuficiente");
    }

    @Test
    void depositoExitoso() throws Exception {
        int codigo = cli.ejecutar(new String[]{"depositar", TARJETA, "0170001100000000000003", "250,75"});

        assertThat(codigo).isEqualTo(AtmCli.EXITO);
        assertThat(salida()).isEqualTo("Depósito exitoso");
        assertThat(cliente.ultimoImporte).isEqualByComparingTo("250.75");
    }

    @Test
    void depositoRechazadoMuestraDetalles() throws Exception {
        cliente.error = new OperacionRechazadaException(400, "DATOS_INVALIDOS", "Los datos de la solicitud son invalidos",
                Collections.singletonList("cbu: debe tener 22 digitos"));

        cli.ejecutar(new String[]{"depositar", TARJETA, "0170001100000000000003", "10"});

        assertThat(salida()).contains("Error: Los datos de la solicitud son invalidos").contains("- cbu: debe tener 22 digitos");
    }

    @Test
    void consultaDeSaldoConFormatoArgentino() throws Exception {
        cliente.saldo = new BigDecimal("99999.99");

        int codigo = cli.ejecutar(new String[]{"saldo", TARJETA, "1000000001"});

        assertThat(codigo).isEqualTo(AtmCli.EXITO);
        assertThat(salida()).isEqualTo("Su saldo es $ 99999,99");
    }

    @Test
    void resultadoInciertoAdvierteVerificarElSaldo() throws Exception {
        cliente.error = new ServicioNoDisponibleException(
                "No se pudo confirmar el resultado de la operacion. Consulte su saldo antes de reintentar (referencia abc)",
                true, "abc", null);

        int codigo = cli.ejecutar(new String[]{"extraer", TARJETA, "1000000001", "100"});

        assertThat(codigo).isEqualTo(AtmCli.SERVICIO_NO_DISPONIBLE);
        assertThat(salida()).contains("Consulte su saldo antes de reintentar");
    }

    @Test
    void usoIncorrectoMuestraAyudaYNoLlamaAlServicio() throws Exception {
        int codigo = cli.ejecutar(new String[]{"extraer", TARJETA});

        assertThat(codigo).isEqualTo(AtmCli.USO_INCORRECTO);
        assertThat(salida()).contains("Error:").contains("Uso:").contains("atm saldo <tarjeta> <cuenta>");
        assertThat(cliente.invocaciones).isZero();
    }

    private static final class ClienteFalso implements CajeroClient {

        private RuntimeException error;
        private BigDecimal saldo = BigDecimal.ZERO;
        private BigDecimal ultimoImporte;
        private int invocaciones;

        private void registrar() {
            invocaciones++;
            if (error != null) {
                throw error;
            }
        }

        @Override
        public void login(String tarjeta) {
            registrar();
        }

        @Override
        public void extraer(String tarjeta, String cuenta, BigDecimal importe) {
            ultimoImporte = importe;
            registrar();
        }

        @Override
        public void depositar(String tarjeta, String cbu, BigDecimal importe) {
            ultimoImporte = importe;
            registrar();
        }

        @Override
        public BigDecimal consultarSaldo(String tarjeta, String cuenta) {
            registrar();
            return saldo;
        }
    }
}
