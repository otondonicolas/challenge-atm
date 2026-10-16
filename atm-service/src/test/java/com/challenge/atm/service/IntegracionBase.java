package com.challenge.atm.service;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(properties = "atm.log-dir=target/test-logs")
@AutoConfigureMockMvc
@Sql(scripts = {"/limpiar.sql", "/data.sql"})
public abstract class IntegracionBase {

    protected static final String TARJETA_ACTIVA = "4517660000000001";
    protected static final String TARJETA_ACTIVA_2 = "4517660000000002";
    protected static final String TARJETA_INACTIVA = "4517660000000003";
    protected static final String TARJETA_INEXISTENTE = "4517669999999999";
    protected static final String CUENTA_PRINCIPAL = "1000000001";
    protected static final String CUENTA_SECUNDARIA = "1000000002";
    protected static final String CUENTA_TERCERO = "1000000003";
    protected static final String CUENTA_TARJETA_INACTIVA = "1000000004";
    protected static final String CBU_PRINCIPAL = "0170001100000000000001";
    protected static final String CBU_TERCERO = "0170001100000000000003";
    protected static final String CBU_SIN_TARJETA = "0170001100000000000005";
    protected static final String CBU_INEXISTENTE = "0170001199999999999999";
}
