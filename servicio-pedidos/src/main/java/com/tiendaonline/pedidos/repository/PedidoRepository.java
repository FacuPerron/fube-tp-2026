package com.tiendaonline.pedidos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido,UUID>{
    List<Pedido> findByIdCliente(UUID idCliente);
    List<Pedido> findByEstadoAndFechaCreacionBefore(EstadoPedido estado, LocalDateTime fechaLimite);
}