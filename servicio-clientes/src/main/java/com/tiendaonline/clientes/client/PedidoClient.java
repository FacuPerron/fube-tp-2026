package com.tiendaonline.clientes.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class PedidoClient {
    private final RestClient restClient;

    public PedidoClient(@Value("${servicios.pedidos.url}")String baseUrl){
        this.restClient = RestClient.create(baseUrl);
    }

    public List<PedidoResponse> obtenerPedidosPorCliente(UUID idCliente) {
        PedidoResponse[] pedidos = restClient.get().uri("/pedidos?idCliente={idCliente}", idCliente)
                                .retrieve().body(PedidoResponse[].class);

        return pedidos != null ? Arrays.asList(pedidos) : List.of();
    }

    public record PedidoResponse(UUID idPedido,UUID idCliente,String estado,BigDecimal montoTotal,
                    LocalDateTime fechaCreacion,LocalDateTime fechaActualizacion,List<ItemPedidoResponse> items){}

    public record ItemPedidoResponse(UUID idItemPedido,UUID idProducto,
                    Integer cantidad,BigDecimal precioUnitario) {}
}
