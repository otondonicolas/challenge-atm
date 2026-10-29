package com.challenge.atm.cli;

import com.challenge.atm.cli.client.CajeroClient;
import com.challenge.atm.cli.client.impl.CajeroClientImpl;
import com.challenge.atm.cli.command.Comando;
import com.challenge.atm.cli.command.ParserComandos;
import com.challenge.atm.cli.command.TipoComando;
import com.challenge.atm.cli.config.Configuracion;
import com.challenge.atm.cli.constants.Constants;
import com.challenge.atm.cli.exception.ArgumentosInvalidosException;
import com.challenge.atm.cli.exception.OperacionRechazadaException;
import com.challenge.atm.cli.exception.ServicioNoDisponibleException;
import com.challenge.atm.cli.util.FormatoMoneda;

import java.io.Console;
import java.io.PrintWriter;

public final class AtmCli {

    public static final int EXITO = 0;
    public static final int OPERACION_RECHAZADA = 1;
    public static final int USO_INCORRECTO = 2;
    public static final int SERVICIO_NO_DISPONIBLE = 3;

    private static final String USO = String.join(System.lineSeparator(),
            Constants.USO,
            String.format(Constants.LINEA_USO, TipoComando.LOGIN.getSintaxis()),
            String.format(Constants.LINEA_USO, TipoComando.EXTRAER.getSintaxis()),
            String.format(Constants.LINEA_USO, TipoComando.DEPOSITAR.getSintaxis()),
            String.format(Constants.LINEA_USO, TipoComando.SALDO.getSintaxis()));

    private final CajeroClient cliente;
    private final PrintWriter salida;

    public AtmCli(CajeroClient cliente, PrintWriter salida) {
        this.cliente = cliente;
        this.salida = salida;
    }

    public static void main(String[] args) {
        AtmCli cli = new AtmCli(new CajeroClientImpl(Configuracion.desdeEntorno()), salidaEstandar());
        System.exit(cli.ejecutar(args));
    }

    private static PrintWriter salidaEstandar() {
        Console consola = System.console();
        return consola != null ? consola.writer() : new PrintWriter(System.out, true);
    }

    public int ejecutar(String[] args) {
        Comando comando;
        try {
            comando = ParserComandos.parsear(args);
        } catch (ArgumentosInvalidosException e) {
            salida.println(String.format(Constants.ERROR, e.getMessage()));
            salida.println(USO);
            return USO_INCORRECTO;
        }

        try {
            salida.println(ejecutar(comando));
            return EXITO;
        } catch (OperacionRechazadaException e) {
            salida.println(mensajeDeRechazo(comando, e));
            return OPERACION_RECHAZADA;
        } catch (ServicioNoDisponibleException e) {
            if (comando.getTipo() == TipoComando.LOGIN) {
                salida.println(Constants.INGRESO_NO_EXITOSO);
            }
            salida.println(String.format(Constants.ERROR, e.getMessage()));
            return SERVICIO_NO_DISPONIBLE;
        }
    }

    private String ejecutar(Comando comando) {
        switch (comando.getTipo()) {
            case LOGIN:
                cliente.login(comando.getTarjeta());
                return Constants.INGRESO_EXITOSO;
            case EXTRAER:
                cliente.extraer(comando.getTarjeta(), comando.getCuenta(), comando.getImporte());
                return Constants.RETIRE_SU_DINERO;
            case DEPOSITAR:
                cliente.depositar(comando.getTarjeta(), comando.getCbu(), comando.getImporte());
                return Constants.DEPOSITO_EXITOSO;
            case SALDO:
                return String.format(Constants.SU_SALDO_ES, FormatoMoneda.formatear(cliente.consultarSaldo(comando.getTarjeta(), comando.getCuenta())));
            default:
                throw new IllegalStateException(String.format(Constants.OPERACION_NO_SOPORTADA_TIPO, comando.getTipo()));
        }
    }

    private static String mensajeDeRechazo(Comando comando, OperacionRechazadaException e) {
        if (comando.getTipo() == TipoComando.LOGIN) {
            return Constants.INGRESO_NO_EXITOSO;
        }
        StringBuilder mensaje = new StringBuilder(String.format(Constants.ERROR, e.getMessage()));
        for (String detalle : e.getDetalles()) {
            mensaje.append(System.lineSeparator()).append(String.format(Constants.DETALLE, detalle));
        }
        return mensaje.toString();
    }
}
