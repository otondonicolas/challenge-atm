package com.challenge.atm.cli.command;

import com.challenge.atm.cli.exception.ArgumentosInvalidosException;

import java.util.Locale;

public enum TipoComando {

    LOGIN("login", "login <tarjeta>", 1),
    EXTRAER("extraer", "extraer <tarjeta> <cuenta> <importe>", 3),
    DEPOSITAR("depositar", "depositar <tarjeta> <cbu> <importe>", 3),
    SALDO("saldo", "saldo <tarjeta> <cuenta>", 2);

    private final String nombre;
    private final String sintaxis;
    private final int cantidadParametros;

    TipoComando(String nombre, String sintaxis, int cantidadParametros) {
        this.nombre = nombre;
        this.sintaxis = sintaxis;
        this.cantidadParametros = cantidadParametros;
    }

    public static TipoComando desdeNombre(String nombre) {
        String normalizado = nombre.toLowerCase(Locale.ROOT);
        for (TipoComando tipo : values()) {
            if (tipo.nombre.equals(normalizado)) {
                return tipo;
            }
        }
        throw new ArgumentosInvalidosException("operacion desconocida '" + nombre + "'");
    }

    public String getSintaxis() {
        return sintaxis;
    }

    public int getCantidadParametros() {
        return cantidadParametros;
    }
}
