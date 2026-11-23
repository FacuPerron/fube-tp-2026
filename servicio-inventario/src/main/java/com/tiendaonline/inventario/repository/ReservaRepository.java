package com.tiendaonline.inventario.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.inventario.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    boolean existsByIdReserva(UUID idReserva);

}
