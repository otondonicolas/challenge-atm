package com.challenge.atm.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HttpCajeroClient implements CajeroClient {

    private static final String RUTA_BASE = "/api/v1/cajero";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String MENSAJE_NO_DISPONIBLE = "El servicio no se encuentra disponible. Intente nuevamente mas tarde";
    private static final String MENSAJE_INCIERTO =
            "No se pudo confirmar el resultado de la operacion. Consulte su saldo antes de reintentar (referencia %s)";
    private static final String MENSAJE_RESPUESTA_INVALIDA = "El servicio devolvio una respuesta invalida";

    private final Configuracion configuracion;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpCajeroClient(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public void login(String tarjeta) {
        ObjectNode cuerpo = objectMapper.createObjectNode().put("tarjeta", tarjeta);
        post("/login", cuerpo, false);
    }

    @Override
    public void extraer(String tarjeta, String cuenta, BigDecimal importe) {
        ObjectNode cuerpo = objectMapper.createObjectNode()
                .put("tarjeta", tarjeta)
                .put("cuenta", cuenta)
                .put("importe", importe);
        post("/extracciones", cuerpo, true);
    }

    @Override
    public void depositar(String tarjeta, String cbu, BigDecimal importe) {
        ObjectNode cuerpo = objectMapper.createObjectNode()
                .put("tarjeta", tarjeta)
                .put("cbu", cbu)
                .put("importe", importe);
        post("/depositos", cuerpo, true);
    }

    @Override
    public BigDecimal consultarSaldo(String tarjeta, String cuenta) {
        ObjectNode cuerpo = objectMapper.createObjectNode()
                .put("tarjeta", tarjeta)
                .put("cuenta", cuenta);
        JsonNode saldo = post("/saldo", cuerpo, false).path("saldo");
        if (!saldo.isNumber()) {
            throw new ServicioNoDisponibleException(MENSAJE_RESPUESTA_INVALIDA, false, null, null);
        }
        return saldo.decimalValue();
    }

    private JsonNode post(String ruta, JsonNode cuerpo, boolean modificaSaldo) {
        String requestId = UUID.randomUUID().toString();
        HttpURLConnection conexion = null;
        boolean solicitudEnviada = false;
        try {
            conexion = (HttpURLConnection) new URL(configuracion.getUrlServicio() + RUTA_BASE + ruta).openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(configuracion.getConnectTimeoutMs());
            conexion.setReadTimeout(configuracion.getReadTimeoutMs());
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setRequestProperty(HEADER_REQUEST_ID, requestId);
            try (OutputStream salida = conexion.getOutputStream()) {
                objectMapper.writeValue(salida, cuerpo);
            }
            solicitudEnviada = true;
            int status = conexion.getResponseCode();
            JsonNode respuesta = leer(status >= 400 ? conexion.getErrorStream() : conexion.getInputStream());
            if (status >= 200 && status < 300) {
                return respuesta;
            }
            throw rechazo(status, respuesta);
        } catch (IOException e) {
            if (solicitudEnviada && modificaSaldo) {
                throw new ServicioNoDisponibleException(String.format(MENSAJE_INCIERTO, requestId), true, requestId, e);
            }
            throw new ServicioNoDisponibleException(MENSAJE_NO_DISPONIBLE, false, requestId, e);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private JsonNode leer(InputStream entrada) throws IOException {
        if (entrada == null) {
            return objectMapper.createObjectNode();
        }
        try (InputStream cuerpo = entrada) {
            return objectMapper.readTree(cuerpo);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private static OperacionRechazadaException rechazo(int status, JsonNode respuesta) {
        String codigo = respuesta.path("codigo").asText("HTTP_" + status);
        String mensaje = respuesta.path("mensaje").asText("La operacion fue rechazada (HTTP " + status + ")");
        List<String> detalles = new ArrayList<>();
        for (JsonNode detalle : respuesta.path("detalles")) {
            detalles.add(detalle.asText());
        }
        return new OperacionRechazadaException(status, codigo, mensaje, detalles);
    }
}
