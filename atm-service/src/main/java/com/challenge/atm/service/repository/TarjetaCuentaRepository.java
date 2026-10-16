package com.challenge.atm.service.repository;

import com.challenge.atm.service.domain.TarjetaCuenta;
import com.challenge.atm.service.domain.TarjetaCuentaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarjetaCuentaRepository extends JpaRepository<TarjetaCuenta, TarjetaCuentaId> {
}
