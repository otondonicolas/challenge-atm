package com.challenge.atm.cli;

public final class Configuracion {

    static final String URL_POR_DEFECTO = "http://localhost:8080";
    static final int CONNECT_TIMEOUT_POR_DEFECTO_MS = 3000;
    static final int READ_TIMEOUT_POR_DEFECTO_MS = 10000;

    private final String urlServicio;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public Configuracion(String urlServicio, int connectTimeoutMs, int readTimeoutMs) {
        this.urlServicio = urlServicio.endsWith("/") ? urlServicio.substring(0, urlServicio.length() - 1) : urlServicio;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public static Configuracion desdeEntorno() {
        return new Configuracion(
                leer("atm.url", "ATM_SERVICE_URL", URL_POR_DEFECTO),
                leerEntero("atm.connect-timeout-ms", "ATM_CONNECT_TIMEOUT_MS", CONNECT_TIMEOUT_POR_DEFECTO_MS),
                leerEntero("atm.read-timeout-ms", "ATM_READ_TIMEOUT_MS", READ_TIMEOUT_POR_DEFECTO_MS));
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
