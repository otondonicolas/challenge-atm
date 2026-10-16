package com.challenge.atm.cli;

import java.io.PrintStream;

public final class AtmCli {

    public static final int EXITO = 0;
    public static final int OPERACION_RECHAZADA = 1;
    public static final int USO_INCORRECTO = 2;
    public static final int SERVICIO_NO_DISPONIBLE = 3;

    private static final String USO = String.join(System.lineSeparator(),
            "Uso:",
            "  atm " + TipoComando.LOGIN.getSintaxis(),
            "  atm " + TipoComando.EXTRAER.getSintaxis(),
            "  atm " + TipoComando.DEPOSITAR.getSintaxis(),
            "  atm " + TipoComando.SALDO.getSintaxis());

    private final CajeroClient cliente;
    private final PrintStream salida;

    public AtmCli(CajeroClient cliente, PrintStream salida) {
        this.cliente = cliente;
        this.salida = salida;
    }

    public static void main(String[] args) {
        AtmCli cli = new AtmCli(new HttpCajeroClient(Configuracion.desdeEntorno()), System.out);
        System.exit(cli.ejecutar(args));
    }

    public int ejecutar(String[] args) {
        Comando comando;
        try {
            comando = ParserComandos.parsear(args);
        } catch (ArgumentosInvalidosException e) {
            salida.println("Error: " + e.getMessage());
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
                salida.println("Ingreso no exitoso");
            }
            salida.println("Error: " + e.getMessage());
            return SERVICIO_NO_DISPONIBLE;
        }
    }

    private String ejecutar(Comando comando) {
        switch (comando.getTipo()) {
            case LOGIN:
                cliente.login(comando.getTarjeta());
                return "Ingreso exitoso";
            case EXTRAER:
                cliente.extraer(comando.getTarjeta(), comando.getCuenta(), comando.getImporte());
                return "Retire su dinero";
            case DEPOSITAR:
                cliente.depositar(comando.getTarjeta(), comando.getCbu(), comando.getImporte());
                return "Depósito exitoso";
            case SALDO:
                return "Su saldo es " + FormatoMoneda.formatear(cliente.consultarSaldo(comando.getTarjeta(), comando.getCuenta()));
            default:
                throw new IllegalStateException("Operacion no soportada: " + comando.getTipo());
        }
    }

    private static String mensajeDeRechazo(Comando comando, OperacionRechazadaException e) {
        if (comando.getTipo() == TipoComando.LOGIN) {
            return "Ingreso no exitoso";
        }
        StringBuilder mensaje = new StringBuilder("Error: ").append(e.getMessage());
        for (String detalle : e.getDetalles()) {
            mensaje.append(System.lineSeparator()).append("  - ").append(detalle);
        }
        return mensaje.toString();
    }
}
