package com.challenge.atm.service.audit;

import com.challenge.atm.service.constants.Constants;
import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.service.ResultadoMovimiento;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({Constants.CAMPO_FECHA_HORA, Constants.CAMPO_TRANSACCION_ID, Constants.CAMPO_REQUEST_ID,
        Constants.CAMPO_OPERACION, Constants.CAMPO_RESULTADO, Constants.CAMPO_CODIGO_ERROR, Constants.CAMPO_TARJETA,
        Constants.CAMPO_CUENTA, Constants.CAMPO_CBU, Constants.CAMPO_IMPORTE, Constants.CAMPO_SALDO_ANTERIOR,
        Constants.CAMPO_SALDO_POSTERIOR})
public final class RegistroTransaccion {

    private final String fechaHora;
    private final String transaccionId;
    private final String requestId;
    private final TipoOperacion operacion;
    private final ResultadoTransaccion resultado;
    private final CodigoError codigoError;
    private final String tarjeta;
    private final String cuenta;
    private final String cbu;
    private final BigDecimal importe;
    private final BigDecimal saldoAnterior;
    private final BigDecimal saldoPosterior;

    private RegistroTransaccion(Builder builder, String fechaHora, String requestId) {
        this.fechaHora = fechaHora;
        this.transaccionId = builder.transaccionId;
        this.requestId = requestId;
        this.operacion = builder.operacion;
        this.resultado = builder.resultado;
        this.codigoError = builder.codigoError;
        this.tarjeta = builder.tarjeta;
        this.cuenta = builder.cuenta;
        this.cbu = builder.cbu;
        this.importe = builder.importe;
        this.saldoAnterior = builder.saldoAnterior;
        this.saldoPosterior = builder.saldoPosterior;
    }

    public static Builder nuevo(String transaccionId, TipoOperacion operacion) {
        return new Builder(transaccionId, operacion);
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getTransaccionId() {
        return transaccionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public TipoOperacion getOperacion() {
        return operacion;
    }

    public ResultadoTransaccion getResultado() {
        return resultado;
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public String getCuenta() {
        return cuenta;
    }

    public String getCbu() {
        return cbu;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public static final class Builder {

        private final String transaccionId;
        private final TipoOperacion operacion;
        private ResultadoTransaccion resultado;
        private CodigoError codigoError;
        private String tarjeta;
        private String cuenta;
        private String cbu;
        private BigDecimal importe;
        private BigDecimal saldoAnterior;
        private BigDecimal saldoPosterior;

        private Builder(String transaccionId, TipoOperacion operacion) {
            this.transaccionId = transaccionId;
            this.operacion = operacion;
        }

        public Builder tarjeta(String tarjeta) {
            this.tarjeta = enmascararTarjeta(tarjeta);
            return this;
        }

        public Builder cuenta(String cuenta) {
            this.cuenta = cuenta;
            return this;
        }

        public Builder cbu(String cbu) {
            this.cbu = cbu;
            return this;
        }

        public Builder importe(BigDecimal importe) {
            this.importe = importe;
            return this;
        }

        public Builder aprobada(ResultadoMovimiento movimiento) {
            this.resultado = ResultadoTransaccion.APROBADA;
            if (movimiento != null) {
                this.cuenta = movimiento.getCuenta();
                this.saldoAnterior = movimiento.getSaldoAnterior();
                this.saldoPosterior = movimiento.getSaldoPosterior();
            }
            return this;
        }

        public Builder rechazada(CodigoError codigo) {
            this.resultado = ResultadoTransaccion.RECHAZADA;
            this.codigoError = codigo;
            return this;
        }

        public Builder fallida(CodigoError codigo) {
            this.resultado = ResultadoTransaccion.ERROR;
            this.codigoError = codigo;
            return this;
        }

        public String getTransaccionId() {
            return transaccionId;
        }

        public RegistroTransaccion construir(String fechaHora, String requestId) {
            return new RegistroTransaccion(this, fechaHora, requestId);
        }

        static String enmascararTarjeta(String numero) {
            if (numero == null || numero.length() < 10) {
                return numero;
            }
            StringBuilder enmascarado = new StringBuilder(numero.substring(0, 6));
            for (int i = 6; i < numero.length() - 4; i++) {
                enmascarado.append('*');
            }
            return enmascarado.append(numero.substring(numero.length() - 4)).toString();
        }
    }
}
