package com.challenge.atm.service.repository;

import com.challenge.atm.service.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numero = :numero")
    Optional<Cuenta> findByNumeroParaActualizar(@Param("numero") String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.cbu = :cbu")
    Optional<Cuenta> findByCbuParaActualizar(@Param("cbu") String cbu);
}
