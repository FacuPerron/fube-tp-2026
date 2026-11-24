package com.tiendaonline.pedidos.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ItemPedidoDTORequest {
    
    @NotNull(message = "El id del producto es obligatorio.")
    private UUID idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El precioUnitario es obligatorio")
    private BigDecimal precioUnitario;
}
