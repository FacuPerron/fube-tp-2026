package com.tiendaonline.pedidos.batch;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.Pedido;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiracionPedidosProcessor implements ItemProcessor<Pedido, Pedido> {

    private final InventarioClient inventarioClient;

    @Override
    public Pedido process(Pedido pedido) {
        try {
            // Liberamos las reservas del pedido
            inventarioClient.liberarReservasPorPedido(pedido.getIdPedido());
        } catch (RuntimeException ex) {
            log.error("Error al liberar reservas del pedido {}: {}", pedido.getIdPedido(), ex.getMessage());
            return null;
        }

        // Actualizamos el estado del pedido para retornarlo
        pedido.setEstado(EstadoPedido.EXPIRADO);
        pedido.setFechaActualizacion(LocalDateTime.now());

        log.info("Pedido expirado por batch {}", pedido.getIdPedido());
        return pedido;
    }
}
