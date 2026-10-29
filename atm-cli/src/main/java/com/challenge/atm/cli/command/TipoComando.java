package com.challenge.atm.cli.command;

import com.challenge.atm.cli.constants.Constants;
import com.challenge.atm.cli.exception.ArgumentosInvalidosException;

import java.util.Locale;

public enum TipoComando {

    LOGIN(Constants.COMANDO_LOGIN, Constants.SINTAXIS_LOGIN, 1),
    EXTRAER(Constants.COMANDO_EXTRAER, Constants.SINTAXIS_EXTRAER, 3),
    DEPOSITAR(Constants.COMANDO_DEPOSITAR, Constants.SINTAXIS_DEPOSITAR, 3),
    SALDO(Constants.COMANDO_SALDO, Constants.SINTAXIS_SALDO, 2);

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
        throw new ArgumentosInvalidosException(String.format(Constants.OPERACION_DESCONOCIDA, nombre));
    }

    public String getSintaxis() {
        return sintaxis;
    }

    public int getCantidadParametros() {
        return cantidadParametros;
    }
}
