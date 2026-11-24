package com.tiendaonline.pedidos.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tiendaonline.pedidos.model.ItemPedido;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido,UUID>{
    List<ItemPedido> findByIdPedido(UUID idPedido);
}
