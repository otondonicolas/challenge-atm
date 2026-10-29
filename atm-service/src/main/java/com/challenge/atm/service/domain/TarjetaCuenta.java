package com.challenge.atm.service.domain;

import com.challenge.atm.service.constants.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = Constants.TABLA_TARJETA_CUENTA)
@IdClass(TarjetaCuentaId.class)
public class TarjetaCuenta {

    @Id
    @Column(name = Constants.COLUMNA_TARJETA, length = 19)
    private String tarjeta;

    @Id
    @Column(name = Constants.COLUMNA_CUENTA, length = 20)
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
