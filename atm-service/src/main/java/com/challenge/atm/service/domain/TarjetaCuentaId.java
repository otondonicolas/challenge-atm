package com.challenge.atm.service.domain;

import java.io.Serializable;
import java.util.Objects;

public class TarjetaCuentaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tarjeta;
    private String cuenta;

    public TarjetaCuentaId() {
    }

    public TarjetaCuentaId(String tarjeta, String cuenta) {
        this.tarjeta = tarjeta;
        this.cuenta = cuenta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TarjetaCuentaId)) {
            return false;
        }
        TarjetaCuentaId otro = (TarjetaCuentaId) o;
        return Objects.equals(tarjeta, otro.tarjeta) && Objects.equals(cuenta, otro.cuenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tarjeta, cuenta);
    }
}
