package com.challenge.atm.service.repository;

import com.challenge.atm.service.constants.Constants;
import com.challenge.atm.service.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(Constants.QUERY_CUENTA_POR_NUMERO)
    Optional<Cuenta> findByNumeroParaActualizar(@Param(Constants.PARAMETRO_NUMERO) String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(Constants.QUERY_CUENTA_POR_CBU)
    Optional<Cuenta> findByCbuParaActualizar(@Param(Constants.PARAMETRO_CBU) String cbu);
}
