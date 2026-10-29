package com.challenge.atm.service.domain;

import com.challenge.atm.service.constants.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = Constants.TABLA_TARJETA)
public class Tarjeta {

    @Id
    @Column(name = Constants.COLUMNA_NUMERO, length = 19)
    private String numero;

    @Column(name = Constants.COLUMNA_ACTIVA, length = 1, nullable = false)
    private String activa;

    protected Tarjeta() {
    }

    public Tarjeta(String numero, boolean activa) {
        this.numero = numero;
        this.activa = activa ? Constants.TARJETA_ACTIVA : Constants.TARJETA_NO_ACTIVA;
    }

    public String getNumero() {
        return numero;
    }

    public boolean estaActiva() {
        return Constants.TARJETA_ACTIVA.equals(activa);
    }
}
