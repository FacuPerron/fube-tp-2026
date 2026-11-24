package com.tiendaonline.pedidos.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido,UUID>{
    List<Pedido> findByIdCliente(UUID idCliente);
}