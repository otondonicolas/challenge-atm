package com.challenge.atm.service.web;

import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import com.challenge.atm.service.web.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(OperacionRechazadaException.class)
    public ResponseEntity<ErrorResponse> operacionRechazada(OperacionRechazadaException e) {
        LOG.info("Operacion rechazada: {}", e.getCodigo());
        return respuesta(e.getCodigo(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(ManejadorErrores::describir)
                .sorted()
                .collect(Collectors.toList());
        LOG.warn("Solicitud invalida: {}", detalles);
        return respuesta(CodigoError.DATOS_INVALIDOS, detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ErrorResponse> cuerpoIlegible(Exception e) {
        LOG.warn("Cuerpo de solicitud ilegible: {}", e.getClass().getSimpleName());
        return respuesta(CodigoError.DATOS_INVALIDOS, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoSoportado(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(CodigoError.DATOS_INVALIDOS.name(), "Metodo HTTP no soportado", null));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> recursoInexistente(NoHandlerFoundException e) {
        return respuesta(CodigoError.RECURSO_INEXISTENTE, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception e) {
        LOG.error("Error inesperado procesando la solicitud", e);
        return respuesta(CodigoError.ERROR_INTERNO, null);
    }

    private static ResponseEntity<ErrorResponse> respuesta(CodigoError codigo, List<String> detalles) {
        return ResponseEntity.status(codigo.getStatus())
                .body(new ErrorResponse(codigo.name(), codigo.getMensaje(), detalles));
    }

    private static String describir(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
