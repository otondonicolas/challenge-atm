package com.challenge.atm.cli;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ParserComandos {

    private static final Pattern TARJETA = Pattern.compile("\\d{16,19}");
    private static final Pattern CUENTA = Pattern.compile("\\d{1,20}");
    private static final Pattern CBU = Pattern.compile("\\d{22}");
    private static final Pattern IMPORTE = Pattern.compile("\\d{1,13}([.,]\\d{1,2})?");

    private ParserComandos() {
    }

    public static Comando parsear(String[] args) {
        if (args == null || args.length == 0) {
            throw new ArgumentosInvalidosException("debe indicar una operacion");
        }
        TipoComando tipo = TipoComando.desdeNombre(args[0]);
        if (args.length - 1 != tipo.getCantidadParametros()) {
            throw new ArgumentosInvalidosException("cantidad de parametros incorrecta. Uso: atm " + tipo.getSintaxis());
        }
        String tarjeta = validar(args[1], TARJETA, "la tarjeta debe tener entre 16 y 19 digitos");
        switch (tipo) {
            case LOGIN:
                return Comando.login(tarjeta);
            case EXTRAER:
                return Comando.extraer(tarjeta, validarCuenta(args[2]), parsearImporte(args[3]));
            case DEPOSITAR:
                return Comando.depositar(tarjeta, validar(args[2], CBU, "el CBU debe tener 22 digitos"), parsearImporte(args[3]));
            case SALDO:
                return Comando.saldo(tarjeta, validarCuenta(args[2]));
            default:
                throw new ArgumentosInvalidosException("operacion no soportada");
        }
    }

    static BigDecimal parsearImporte(String valor) {
        String importe = valor.trim();
        if (!IMPORTE.matcher(importe).matches()) {
            throw new ArgumentosInvalidosException("importe invalido '" + valor + "'. Use el formato 1500 o 1500,50");
        }
        BigDecimal resultado = new BigDecimal(importe.replace(',', '.'));
        if (resultado.signum() <= 0) {
            throw new ArgumentosInvalidosException("el importe debe ser mayor a cero");
        }
        return resultado;
    }

    private static String validarCuenta(String valor) {
        return validar(valor, CUENTA, "la cuenta debe tener entre 1 y 20 digitos");
    }

    private static String validar(String valor, Pattern formato, String mensaje) {
        String limpio = valor.trim();
        if (!formato.matcher(limpio).matches()) {
            throw new ArgumentosInvalidosException(mensaje);
        }
        return limpio;
    }
}
