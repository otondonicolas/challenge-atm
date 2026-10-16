package com.challenge.atm.service.service;

import com.challenge.atm.service.IntegracionBase;
import com.challenge.atm.service.exception.CodigoError;
import com.challenge.atm.service.exception.OperacionRechazadaException;
import com.challenge.atm.service.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrenciaExtraccionTest extends IntegracionBase {

    private static final int HILOS = 20;

    @Autowired
    private CajeroService cajeroService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void extraccionesSimultaneasNuncaDejanSaldoNegativoNiPierdenActualizaciones() throws Exception {
        jdbcTemplate.update("UPDATE cuenta SET saldo = 1000.00 WHERE numero = ?", CUENTA_PRINCIPAL);
        ExecutorService pool = Executors.newFixedThreadPool(HILOS);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < HILOS; i++) {
            resultados.add(pool.submit(extraccion(largada, new BigDecimal("100.00"))));
        }
        largada.countDown();

        int aprobadas = 0;
        int rechazadas = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(30, TimeUnit.SECONDS)) {
                aprobadas++;
            } else {
                rechazadas++;
            }
        }
        pool.shutdown();

        assertThat(aprobadas).isEqualTo(10);
        assertThat(rechazadas).isEqualTo(10);
        assertThat(cuentaRepository.findById(CUENTA_PRINCIPAL).get().getSaldo()).isEqualByComparingTo("0.00");
    }

    @Test
    void depositosYExtraccionesSimultaneosMantienenElSaldoConsistente() throws Exception {
        jdbcTemplate.update("UPDATE cuenta SET saldo = 5000.00 WHERE numero = ?", CUENTA_PRINCIPAL);
        ExecutorService pool = Executors.newFixedThreadPool(HILOS);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < HILOS; i++) {
            if (i % 2 == 0) {
                resultados.add(pool.submit(extraccion(largada, new BigDecimal("100.00"))));
            } else {
                resultados.add(pool.submit(deposito(largada, new BigDecimal("50.00"))));
            }
        }
        largada.countDown();
        for (Future<Boolean> resultado : resultados) {
            assertThat(resultado.get(30, TimeUnit.SECONDS)).isTrue();
        }
        pool.shutdown();

        assertThat(cuentaRepository.findById(CUENTA_PRINCIPAL).get().getSaldo()).isEqualByComparingTo("4500.00");
    }

    private Callable<Boolean> extraccion(CountDownLatch largada, BigDecimal importe) {
        return () -> {
            largada.await();
            try {
                cajeroService.extraer(TARJETA_ACTIVA, CUENTA_PRINCIPAL, importe);
                return true;
            } catch (OperacionRechazadaException e) {
                assertThat(e.getCodigo()).isEqualTo(CodigoError.SALDO_INSUFICIENTE);
                return false;
            }
        };
    }

    private Callable<Boolean> deposito(CountDownLatch largada, BigDecimal importe) {
        return () -> {
            largada.await();
            cajeroService.depositar(TARJETA_ACTIVA_2, CBU_PRINCIPAL, importe);
            return true;
        };
    }
}
