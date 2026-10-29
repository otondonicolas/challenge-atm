package com.challenge.atm.cli.client.impl;

import com.challenge.atm.cli.client.CajeroClient;
import com.challenge.atm.cli.constants.Constants;
import com.challenge.atm.cli.config.Configuracion;
import com.challenge.atm.cli.exception.OperacionRechazadaException;
import com.challenge.atm.cli.exception.ServicioNoDisponibleException;
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

public final class CajeroClientImpl implements CajeroClient {

    private final Configuracion configuracion;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CajeroClientImpl(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public void login(String tarjeta) {
        ObjectNode cuerpo = objectMapper.createObjectNode().put(Constants.CAMPO_TARJETA, tarjeta);
        post(Constants.RUTA_LOGIN, cuerpo, false);
    }

    @Override
    public void extraer(String tarjeta, String cuenta, BigDecimal importe) {
        ObjectNode cuerpo = objectMapper.createObjectNode()
                .put(Constants.CAMPO_TARJETA, tarjeta)
                .put(Constants.CAMPO_CUENTA, cuenta)
                .put(Constants.CAMPO_IMPORTE, importe);
        post(Constants.RUTA_EXTRACCIONES, cuerpo, true);
    }

    @Override
    public void depositar(String tarjeta, String cbu, BigDecimal importe) {
        ObjectNode cuerpo = objectMapper.createObjectNode()
                .put(Constants.CAMPO_TARJETA, tarjeta)
                .put(Constants.CAMPO_CBU, cbu)
                .put(Constants.CAMPO_IMPORTE, importe);
        post(Constants.RUTA_DEPOSITOS, cuerpo, true);
    }

    @Override
    public BigDecimal consultarSaldo(String tarjeta, String cuenta) {
        ObjectNode cuerpo = objectMapper.createObjectNode()
                .put(Constants.CAMPO_TARJETA, tarjeta)
                .put(Constants.CAMPO_CUENTA, cuenta);
        JsonNode saldo = post(Constants.RUTA_SALDO, cuerpo, false).path(Constants.CAMPO_SALDO);
        if (!saldo.isNumber()) {
            throw new ServicioNoDisponibleException(Constants.RESPUESTA_INVALIDA, false, null, null);
        }
        return saldo.decimalValue();
    }

    private JsonNode post(String ruta, JsonNode cuerpo, boolean modificaSaldo) {
        String requestId = UUID.randomUUID().toString();
        HttpURLConnection conexion = null;
        boolean solicitudEnviada = false;
        try {
            conexion = (HttpURLConnection) new URL(configuracion.getUrlServicio() + Constants.RUTA_BASE + ruta).openConnection();
            conexion.setRequestMethod(Constants.METODO_POST);
            conexion.setConnectTimeout(configuracion.getConnectTimeoutMs());
            conexion.setReadTimeout(configuracion.getReadTimeoutMs());
            conexion.setDoOutput(true);
            conexion.setRequestProperty(Constants.HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON);
            conexion.setRequestProperty(Constants.HEADER_ACCEPT, Constants.ACCEPT_JSON);
            conexion.setRequestProperty(Constants.HEADER_REQUEST_ID, requestId);
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
                throw new ServicioNoDisponibleException(String.format(Constants.RESULTADO_INCIERTO, requestId), true, requestId, e);
            }
            throw new ServicioNoDisponibleException(Constants.SERVICIO_NO_DISPONIBLE, false, requestId, e);
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
        String codigo = respuesta.path(Constants.CAMPO_CODIGO).asText(String.format(Constants.CODIGO_HTTP, status));
        String mensaje = respuesta.path(Constants.CAMPO_MENSAJE).asText(String.format(Constants.OPERACION_RECHAZADA_HTTP, status));
        List<String> detalles = new ArrayList<>();
        for (JsonNode detalle : respuesta.path(Constants.CAMPO_DETALLES)) {
            detalles.add(detalle.asText());
        }
        return new OperacionRechazadaException(status, codigo, mensaje, detalles);
    }
}
