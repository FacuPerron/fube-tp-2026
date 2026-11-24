package com.tiendaonline.pedidos.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTOResponse {
    private UUID idItemPedido;
    private UUID idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
