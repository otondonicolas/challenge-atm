package com.challenge.atm.cli.client.impl;

import com.challenge.atm.cli.config.Configuracion;
import com.challenge.atm.cli.exception.OperacionRechazadaException;
import com.challenge.atm.cli.exception.ServicioNoDisponibleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CajeroClientImplTest {

    private static final String TARJETA = "4517660000000001";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<JsonNode> cuerpoRecibido = new AtomicReference<>();
    private final AtomicReference<String> requestIdRecibido = new AtomicReference<>();
    private final AtomicReference<String> rutaRecibida = new AtomicReference<>();

    private HttpServer servidor;
    private volatile int statusRespuesta = 200;
    private volatile String cuerpoRespuesta = "{}";
    private volatile long demoraMs;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", this::atender);
        servidor.start();
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    private void atender(HttpExchange intercambio) throws IOException {
        rutaRecibida.set(intercambio.getRequestURI().getPath());
        requestIdRecibido.set(intercambio.getRequestHeaders().getFirst("X-Request-Id"));
        cuerpoRecibido.set(objectMapper.readTree(leer(intercambio.getRequestBody())));
        if (demoraMs > 0) {
            try {
                Thread.sleep(demoraMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] respuesta = cuerpoRespuesta.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(statusRespuesta, respuesta.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(respuesta);
        }
    }

    private static byte[] leer(InputStream entrada) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] bloque = new byte[1024];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            buffer.write(bloque, 0, leidos);
        }
        return buffer.toByteArray();
    }

    private CajeroClientImpl cliente() {
        return new CajeroClientImpl(new Configuracion("http://127.0.0.1:" + servidor.getAddress().getPort(), 1000, 1000));
    }

    @Test
    void enviaLaExtraccionComoJsonConRequestId() {
        cuerpoRespuesta = "{\"transaccionId\":\"t1\",\"mensaje\":\"Retire su dinero\"}";

        cliente().extraer(TARJETA, "1000000001", new BigDecimal("1500.50"));

        assertThat(rutaRecibida.get()).isEqualTo("/api/v1/cajero/extracciones");
        assertThat(requestIdRecibido.get()).isNotBlank();
        assertThat(cuerpoRecibido.get().get("tarjeta").asText()).isEqualTo(TARJETA);
        assertThat(cuerpoRecibido.get().get("cuenta").asText()).isEqualTo("1000000001");
        assertThat(cuerpoRecibido.get().get("importe").decimalValue()).isEqualByComparingTo("1500.50");
    }

    @Test
    void devuelveElSaldoInformadoPorElServicio() {
        cuerpoRespuesta = "{\"transaccionId\":\"t1\",\"cuenta\":\"1000000001\",\"saldo\":2500.50}";

        BigDecimal saldo = cliente().consultarSaldo(TARJETA, "1000000001");

        assertThat(rutaRecibida.get()).isEqualTo("/api/v1/cajero/saldo");
        assertThat(saldo).isEqualByComparingTo("2500.50");
    }

    @Test
    void traduceElErrorDelServicioAOperacionRechazada() {
        statusRespuesta = 422;
        cuerpoRespuesta = "{\"codigo\":\"SALDO_INSUFICIENTE\",\"mensaje\":\"Saldo insuficiente\"}";

        assertThatThrownBy(() -> cliente().extraer(TARJETA, "1000000001", BigDecimal.TEN))
                .isInstanceOf(OperacionRechazadaException.class)
                .hasMessage("Saldo insuficiente")
                .extracting("codigo").isEqualTo("SALDO_INSUFICIENTE");
    }

    @Test
    void toleraRespuestasDeErrorSinCuerpoJson() {
        statusRespuesta = 502;
        cuerpoRespuesta = "<html>Bad Gateway</html>";

        assertThatThrownBy(() -> cliente().login(TARJETA))
                .isInstanceOf(OperacionRechazadaException.class)
                .extracting("codigo").isEqualTo("HTTP_502");
    }

    @Test
    void servicioCaidoNoEsResultadoIncierto() throws IOException {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        }
        CajeroClientImpl cliente = new CajeroClientImpl(new Configuracion("http://127.0.0.1:" + puertoLibre, 500, 500));

        assertThatThrownBy(() -> cliente.extraer(TARJETA, "1000000001", BigDecimal.TEN))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .extracting("resultadoIncierto").isEqualTo(false);
    }

    @Test
    void timeoutDeLecturaEnUnaExtraccionEsResultadoIncierto() {
        demoraMs = 2000;

        assertThatThrownBy(() -> cliente().extraer(TARJETA, "1000000001", BigDecimal.TEN))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .hasMessageContaining("Consulte su saldo")
                .extracting("resultadoIncierto").isEqualTo(true);
    }

    @Test
    void timeoutDeLecturaEnUnaConsultaNoEsResultadoIncierto() {
        demoraMs = 2000;

        assertThatThrownBy(() -> cliente().consultarSaldo(TARJETA, "1000000001"))
                .isInstanceOf(ServicioNoDisponibleException.class)
                .extracting("resultadoIncierto").isEqualTo(false);
    }
}
