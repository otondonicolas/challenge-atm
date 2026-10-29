package com.challenge.atm.cli.config;

import com.challenge.atm.cli.constants.Constants;

public final class Configuracion {

    static final int CONNECT_TIMEOUT_POR_DEFECTO_MS = 3000;
    static final int READ_TIMEOUT_POR_DEFECTO_MS = 10000;

    private final String urlServicio;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public Configuracion(String urlServicio, int connectTimeoutMs, int readTimeoutMs) {
        this.urlServicio = urlServicio.endsWith(Constants.BARRA) ? urlServicio.substring(0, urlServicio.length() - 1) : urlServicio;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public static Configuracion desdeEntorno() {
        return new Configuracion(
                leer(Constants.PROPIEDAD_URL, Constants.VARIABLE_URL, Constants.URL_POR_DEFECTO),
                leerEntero(Constants.PROPIEDAD_CONNECT_TIMEOUT, Constants.VARIABLE_CONNECT_TIMEOUT, CONNECT_TIMEOUT_POR_DEFECTO_MS),
                leerEntero(Constants.PROPIEDAD_READ_TIMEOUT, Constants.VARIABLE_READ_TIMEOUT, READ_TIMEOUT_POR_DEFECTO_MS));
    }

    private static String leer(String propiedad, String variableEntorno, String porDefecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.trim().isEmpty()) {
            valor = System.getenv(variableEntorno);
        }
        return valor == null || valor.trim().isEmpty() ? porDefecto : valor.trim();
    }

    private static int leerEntero(String propiedad, String variableEntorno, int porDefecto) {
        try {
            return Integer.parseInt(leer(propiedad, variableEntorno, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    public String getUrlServicio() {
        return urlServicio;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
}
