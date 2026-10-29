package com.challenge.atm.service.audit;

import com.challenge.atm.service.constants.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AuditoriaTransaccional {

    private static final Logger LOG_TRANSACCIONES = LoggerFactory.getLogger(Constants.LOGGER_AUDITORIA);
    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaTransaccional.class);

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AuditoriaTransaccional(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void registrar(RegistroTransaccion.Builder builder) {
        String fechaHora = OffsetDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        RegistroTransaccion registro = builder.construir(fechaHora, MDC.get(Constants.MDC_REQUEST_ID));
        try {
            LOG_TRANSACCIONES.info(objectMapper.writeValueAsString(registro));
        } catch (JsonProcessingException e) {
            LOG.error(Constants.ERROR_SERIALIZANDO_REGISTRO, registro.getTransaccionId(), e);
        }
    }
}
