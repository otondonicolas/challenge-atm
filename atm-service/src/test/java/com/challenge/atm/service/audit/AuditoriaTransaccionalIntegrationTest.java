package com.challenge.atm.service.audit;

import com.challenge.atm.service.IntegracionBase;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import com.challenge.atm.service.service.CajeroService;
import com.challenge.atm.service.service.ResultadoOperacion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditoriaTransaccionalIntegrationTest extends IntegracionBase {

    private static final Path LOG_TRANSACCIONES = Paths.get("target", "test-logs", "transacciones.log");

    @Autowired
    private CajeroService cajeroService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cadaExtraccionAprobadaDejaUnRegistroJsonConSaldos() throws IOException {
        ResultadoOperacion resultado = cajeroService.extraer(TARJETA_ACTIVA, CUENTA_SECUNDARIA, new BigDecimal("500.50"));

        JsonNode registro = buscarRegistro(resultado.getTransaccionId());

        assertThat(registro.get("operacion").asText()).isEqualTo("EXTRACCION");
        assertThat(registro.get("resultado").asText()).isEqualTo("APROBADA");
        assertThat(registro.get("tarjeta").asText()).isEqualTo("451766******0001");
        assertThat(registro.get("cuenta").asText()).isEqualTo(CUENTA_SECUNDARIA);
        assertThat(registro.get("importe").decimalValue()).isEqualByComparingTo("500.50");
        assertThat(registro.get("saldoAnterior").decimalValue()).isEqualByComparingTo("2500.50");
        assertThat(registro.get("saldoPosterior").decimalValue()).isEqualByComparingTo("2000.00");
        assertThat(registro.get("fechaHora").asText()).isNotBlank();
    }

    @Test
    void lasOperacionesRechazadasTambienSeRegistran() throws IOException {
        String cbuInexistente = CBU_INEXISTENTE;

        assertThatThrownBy(() -> cajeroService.depositar(TARJETA_ACTIVA, cbuInexistente, new BigDecimal("10")))
                .isInstanceOf(OperacionRechazadaException.class);

        List<JsonNode> registros = leerRegistros().stream()
                .filter(r -> cbuInexistente.equals(r.path("cbu").asText()))
                .collect(Collectors.toList());
        assertThat(registros).isNotEmpty();
        JsonNode ultimo = registros.get(registros.size() - 1);
        assertThat(ultimo.get("resultado").asText()).isEqualTo("RECHAZADA");
        assertThat(ultimo.get("codigoError").asText()).isEqualTo("CBU_INEXISTENTE");
        assertThat(ultimo.has("saldoPosterior")).isFalse();
    }

    @Test
    void elNumeroDeTarjetaCompletoNuncaQuedaEnElLog() throws IOException {
        cajeroService.login(TARJETA_ACTIVA);

        String contenido = new String(Files.readAllBytes(LOG_TRANSACCIONES), StandardCharsets.UTF_8);
        assertThat(contenido).doesNotContain(TARJETA_ACTIVA);
    }

    private JsonNode buscarRegistro(String transaccionId) throws IOException {
        return leerRegistros().stream()
                .filter(r -> transaccionId.equals(r.path("transaccionId").asText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontro la transaccion " + transaccionId));
    }

    private List<JsonNode> leerRegistros() throws IOException {
        return Files.readAllLines(LOG_TRANSACCIONES, StandardCharsets.UTF_8).stream()
                .filter(linea -> !linea.trim().isEmpty())
                .map(this::parsear)
                .collect(Collectors.toList());
    }

    private JsonNode parsear(String linea) {
        try {
            return objectMapper.readTree(linea);
        } catch (IOException e) {
            throw new AssertionError("Linea de log transaccional invalida: " + linea, e);
        }
    }
}
