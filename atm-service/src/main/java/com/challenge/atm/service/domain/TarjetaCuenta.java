package com.challenge.atm.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "tarjeta_cuenta")
@IdClass(TarjetaCuentaId.class)
public class TarjetaCuenta {

    @Id
    @Column(name = "tarjeta", length = 19)
    private String tarjeta;

    @Id
    @Column(name = "cuenta", length = 20)
    private String cuenta;

    protected TarjetaCuenta() {
    }

    public TarjetaCuenta(String tarjeta, String cuenta) {
        this.tarjeta = tarjeta;
        this.cuenta = cuenta;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public String getCuenta() {
        return cuenta;
    }
}
