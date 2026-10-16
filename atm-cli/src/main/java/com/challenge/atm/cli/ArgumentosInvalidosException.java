package com.challenge.atm.cli;

public class ArgumentosInvalidosException extends RuntimeException {

    public ArgumentosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
