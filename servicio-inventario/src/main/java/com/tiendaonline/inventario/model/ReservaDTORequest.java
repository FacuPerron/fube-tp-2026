package com.tiendaonline.inventario.model;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor 
public class ReservaDTORequest {

    @NotNull(message = "El ID del producto es obligatorio.")
    private UUID idProducto;

    @NotNull(message = "El ID del pedido es obligatorio.")
    private UUID idPedido;

    @NotNull(message = "La cantidad es obligatoria.") 
    @Positive(message = "La cantidad debe ser positiva.") 
    private Integer cantidad;
}
