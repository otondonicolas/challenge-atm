package com.challenge.atm.cli.command;

import com.challenge.atm.cli.constants.Constants;
import com.challenge.atm.cli.exception.ArgumentosInvalidosException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ParserComandos {

    private static final Pattern TARJETA = Pattern.compile(Constants.REGEX_TARJETA);
    private static final Pattern CUENTA = Pattern.compile(Constants.REGEX_CUENTA);
    private static final Pattern CBU = Pattern.compile(Constants.REGEX_CBU);
    private static final Pattern IMPORTE = Pattern.compile(Constants.REGEX_IMPORTE);

    private ParserComandos() {
    }

    public static Comando parsear(String[] args) {
        if (args == null || args.length == 0) {
            throw new ArgumentosInvalidosException(Constants.DEBE_INDICAR_OPERACION);
        }
        TipoComando tipo = TipoComando.desdeNombre(args[0]);
        if (args.length - 1 != tipo.getCantidadParametros()) {
            throw new ArgumentosInvalidosException(String.format(Constants.CANTIDAD_PARAMETROS_INCORRECTA, tipo.getSintaxis()));
        }
        String tarjeta = validar(args[1], TARJETA, Constants.TARJETA_INVALIDA);
        switch (tipo) {
            case LOGIN:
                return Comando.login(tarjeta);
            case EXTRAER:
                return Comando.extraer(tarjeta, validarCuenta(args[2]), parsearImporte(args[3]));
            case DEPOSITAR:
                return Comando.depositar(tarjeta, validar(args[2], CBU, Constants.CBU_INVALIDO), parsearImporte(args[3]));
            case SALDO:
                return Comando.saldo(tarjeta, validarCuenta(args[2]));
            default:
                throw new ArgumentosInvalidosException(Constants.OPERACION_NO_SOPORTADA);
        }
    }

    static BigDecimal parsearImporte(String valor) {
        String importe = valor.trim();
        if (!IMPORTE.matcher(importe).matches()) {
            throw new ArgumentosInvalidosException(String.format(Constants.IMPORTE_INVALIDO, valor));
        }
        BigDecimal resultado = new BigDecimal(importe.replace(',', '.'));
        if (resultado.signum() <= 0) {
            throw new ArgumentosInvalidosException(Constants.IMPORTE_NO_POSITIVO);
        }
        return resultado;
    }

    private static String validarCuenta(String valor) {
        return validar(valor, CUENTA, Constants.CUENTA_INVALIDA);
    }

    private static String validar(String valor, Pattern formato, String mensaje) {
        String limpio = valor.trim();
        if (!formato.matcher(limpio).matches()) {
            throw new ArgumentosInvalidosException(mensaje);
        }
        return limpio;
    }
}
