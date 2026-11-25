package com.tiendaonline.inventario.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.inventario.model.Reserva;
import com.tiendaonline.inventario.model.enume.EstadoReserva;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    boolean existsByIdReserva(UUID idReserva);
    List<Reserva> findByIdPedidoAndEstado(UUID idPedido, EstadoReserva estado);
}
