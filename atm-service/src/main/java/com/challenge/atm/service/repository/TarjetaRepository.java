package com.challenge.atm.service.repository;

import com.challenge.atm.service.domain.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarjetaRepository extends JpaRepository<Tarjeta, String> {
}
