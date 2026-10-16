package com.challenge.atm.service.web;

import com.challenge.atm.service.IntegracionBase;
import com.challenge.atm.service.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CajeroControllerIntegrationTest extends IntegracionBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CuentaRepository cuentaRepository;

    private ResultActions enviar(String ruta, String json) throws Exception {
        return mockMvc.perform(post("/api/v1/cajero" + ruta).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String saldoDe(String cuenta) {
        return cuentaRepository.findById(cuenta).get().getSaldo().toPlainString();
    }

    @Test
    void loginConTarjetaActivaEsExitoso() throws Exception {
        enviar("/login", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Ingreso exitoso"))
                .andExpect(jsonPath("$.transaccionId").value(notNullValue()));
    }

    @Test
    void loginConTarjetaInexistenteEsRechazado() throws Exception {
        enviar("/login", "{\"tarjeta\":\"" + TARJETA_INEXISTENTE + "\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TARJETA_INEXISTENTE"));
    }

    @Test
    void loginConTarjetaInactivaEsRechazado() throws Exception {
        enviar("/login", "{\"tarjeta\":\"" + TARJETA_INACTIVA + "\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("TARJETA_INACTIVA"));
    }

    @Test
    void loginConFormatoInvalidoDevuelveDetalle() throws Exception {
        enviar("/login", "{\"tarjeta\":\"12AB\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.detalles", hasItem("tarjeta: debe tener entre 16 y 19 digitos")));
    }

    @Test
    void extraccionExitosaDebitaLaCuenta() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_SECUNDARIA + "\",\"importe\":500.50}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Retire su dinero"));

        assertThat(saldoDe(CUENTA_SECUNDARIA)).isEqualTo("2000.00");
    }

    @Test
    void extraccionDeTodoElSaldoDejaLaCuentaEnCero() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_SECUNDARIA + "\",\"importe\":2500.50}")
                .andExpect(status().isOk());

        assertThat(saldoDe(CUENTA_SECUNDARIA)).isEqualTo("0.00");
    }

    @Test
    void extraccionConSaldoInsuficienteNoModificaElSaldo() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_SECUNDARIA + "\",\"importe\":2500.51}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.mensaje").value("Saldo insuficiente"));

        assertThat(saldoDe(CUENTA_SECUNDARIA)).isEqualTo("2500.50");
    }

    @Test
    void extraccionDeCuentaAjenaEsRechazada() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_TERCERO + "\",\"importe\":10}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CUENTA_NO_ASOCIADA"));

        assertThat(saldoDe(CUENTA_TERCERO)).isEqualTo("10000.00");
    }

    @Test
    void extraccionConTarjetaInactivaEsRechazada() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_INACTIVA + "\",\"cuenta\":\"" + CUENTA_TARJETA_INACTIVA + "\",\"importe\":10}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("TARJETA_INACTIVA"));

        assertThat(saldoDe(CUENTA_TARJETA_INACTIVA)).isEqualTo("50000.00");
    }

    @Test
    void extraccionConImporteNegativoEsRechazada() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_PRINCIPAL + "\",\"importe\":-100}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("IMPORTE_INVALIDO"));

        assertThat(saldoDe(CUENTA_PRINCIPAL)).isEqualTo("150000.00");
    }

    @Test
    void extraccionConImporteConTresDecimalesEsRechazada() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_PRINCIPAL + "\",\"importe\":1.001}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("IMPORTE_INVALIDO"));
    }

    @Test
    void depositoEnCuentaPropiaAcreditaElImporte() throws Exception {
        enviar("/depositos", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cbu\":\"" + CBU_PRINCIPAL + "\",\"importe\":1000}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Depósito exitoso"));

        assertThat(saldoDe(CUENTA_PRINCIPAL)).isEqualTo("151000.00");
    }

    @Test
    void depositoEnCuentaDeTerceroAcreditaElImporte() throws Exception {
        enviar("/depositos", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cbu\":\"" + CBU_TERCERO + "\",\"importe\":0.01}")
                .andExpect(status().isOk());

        assertThat(saldoDe(CUENTA_TERCERO)).isEqualTo("10000.01");
    }

    @Test
    void depositoEnCuentaSinTarjetaAsociadaEsValido() throws Exception {
        enviar("/depositos", "{\"tarjeta\":\"" + TARJETA_ACTIVA_2 + "\",\"cbu\":\"" + CBU_SIN_TARJETA + "\",\"importe\":250}")
                .andExpect(status().isOk());

        assertThat(saldoDe("1000000005")).isEqualTo("250.00");
    }

    @Test
    void depositoConCbuInexistenteEsRechazado() throws Exception {
        enviar("/depositos", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cbu\":\"" + CBU_INEXISTENTE + "\",\"importe\":100}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CBU_INEXISTENTE"));
    }

    @Test
    void depositoConTarjetaInactivaEsRechazado() throws Exception {
        enviar("/depositos", "{\"tarjeta\":\"" + TARJETA_INACTIVA + "\",\"cbu\":\"" + CBU_PRINCIPAL + "\",\"importe\":100}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("TARJETA_INACTIVA"));

        assertThat(saldoDe(CUENTA_PRINCIPAL)).isEqualTo("150000.00");
    }

    @Test
    void depositoConCbuMalFormadoDevuelveDetalle() throws Exception {
        enviar("/depositos", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cbu\":\"123\",\"importe\":100}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("cbu: debe tener 22 digitos")));
    }

    @Test
    void consultaDeSaldoDevuelveElSaldoDeLaCuenta() throws Exception {
        enviar("/saldo", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_SECUNDARIA + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuenta").value(CUENTA_SECUNDARIA))
                .andExpect(jsonPath("$.saldo").value(2500.50));
    }

    @Test
    void consultaDeSaldoDeCuentaAjenaEsRechazada() throws Exception {
        enviar("/saldo", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"" + CUENTA_TERCERO + "\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CUENTA_NO_ASOCIADA"));
    }

    @Test
    void cuerpoMalFormadoDevuelveDatosInvalidos() throws Exception {
        enviar("/extracciones", "{\"tarjeta\":\"" + TARJETA_ACTIVA + "\",\"cuenta\":\"1\",\"importe\":\"mucho\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        enviar("/transferencias", "{}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_INEXISTENTE"));
    }

    @Test
    void metodoNoSoportadoDevuelve405() throws Exception {
        mockMvc.perform(get("/api/v1/cajero/saldo"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void elRequestIdRecibidoSeDevuelveEnLaRespuesta() throws Exception {
        mockMvc.perform(post("/api/v1/cajero/login")
                        .header(RequestIdFilter.HEADER_REQUEST_ID, "req-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarjeta\":\"" + TARJETA_ACTIVA + "\"}"))
                .andExpect(header().string(RequestIdFilter.HEADER_REQUEST_ID, "req-123"));
    }

    @Test
    void healthCheckEstaDisponible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
