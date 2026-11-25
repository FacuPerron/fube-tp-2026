package com.tiendaonline.pedidos.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.Pedido;

@ExtendWith(MockitoExtension.class)
public class ExpiracionPedidosProcessorTest {

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private ExpiracionPedidosProcessor processor;

    @Test
    void process_CuandoLiberaReservas_DeberiaPasarPedidoAExpirado() {
        // Test para expiración de pedidos

        UUID idPedido = UUID.randomUUID();

        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setIdCliente(UUID.randomUUID());
        pedido.setEstado(EstadoPedido.CREADO); // -> El pedido tiene que estar en estado CREADO
        pedido.setMontoTotal(new BigDecimal("100.00"));
        pedido.setFechaCreacion(LocalDateTime.now().minusHours(25)); // -> Tiene que tener más de 24hs
        pedido.setFechaActualizacion(LocalDateTime.now().minusHours(25));

        doNothing().when(inventarioClient).liberarReservasPorPedido(idPedido);

        Pedido resultado = processor.process(pedido);

        assertNotNull(resultado);
        assertEquals(EstadoPedido.EXPIRADO, resultado.getEstado());
        verify(inventarioClient, times(1)).liberarReservasPorPedido(idPedido);
    }

    @Test
    void process_CuandoFallaInventario_DeberiaDevolverNullYNoCambiarEstado() {
        // Test para expiración de pedidos cuando falla el servicio de inventario

        UUID idPedido = UUID.randomUUID();

        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setIdCliente(UUID.randomUUID());
        pedido.setEstado(EstadoPedido.CREADO); // -> El pedido tiene que estar en estado CREADO
        pedido.setMontoTotal(new BigDecimal("100.00"));
        pedido.setFechaCreacion(LocalDateTime.now().minusHours(25)); // -> Tiene que tener más de 24hs
        pedido.setFechaActualizacion(LocalDateTime.now().minusHours(25));

        doThrow(new RuntimeException("Error en servicio-inventario"))
                .when(inventarioClient).liberarReservasPorPedido(idPedido);

        Pedido resultado = processor.process(pedido);

        assertNull(resultado);
        assertEquals(EstadoPedido.CREADO, pedido.getEstado());
        verify(inventarioClient, times(1)).liberarReservasPorPedido(idPedido);
    }
}
