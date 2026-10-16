package com.challenge.atm.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tarjeta")
public class Tarjeta {

    private static final String ACTIVA = "S";

    @Id
    @Column(name = "numero", length = 19)
    private String numero;

    @Column(name = "activa", length = 1, nullable = false)
    private String activa;

    protected Tarjeta() {
    }

    public Tarjeta(String numero, boolean activa) {
        this.numero = numero;
        this.activa = activa ? ACTIVA : "N";
    }

    public String getNumero() {
        return numero;
    }

    public boolean estaActiva() {
        return ACTIVA.equals(activa);
    }
}
