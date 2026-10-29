package com.challenge.atm.service.domain;

import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cuenta")
public class Cuenta {

    private static final int DECIMALES = 2;

    @Id
    @Column(name = "numero", length = 20)
    private String numero;

    @Column(name = "cbu", length = 22, nullable = false, unique = true)
    private String cbu;

    @Column(name = "saldo", precision = 15, scale = 2, nullable = false)
    private BigDecimal saldo;

    protected Cuenta() {
    }

    public Cuenta(String numero, String cbu, BigDecimal saldo) {
        this.numero = numero;
        this.cbu = cbu;
        this.saldo = saldo;
    }

    public void debitar(BigDecimal importe) {
        validarImporte(importe);
        if (saldo.compareTo(importe) < 0) {
            throw new OperacionRechazadaException(CodigoError.SALDO_INSUFICIENTE);
        }
        saldo = saldo.subtract(importe);
    }

    public void acreditar(BigDecimal importe) {
        validarImporte(importe);
        saldo = saldo.add(importe);
    }

    private static void validarImporte(BigDecimal importe) {
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > DECIMALES) {
            throw new OperacionRechazadaException(CodigoError.IMPORTE_INVALIDO);
        }
    }

    public String getNumero() {
        return numero;
    }

    public String getCbu() {
        return cbu;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
