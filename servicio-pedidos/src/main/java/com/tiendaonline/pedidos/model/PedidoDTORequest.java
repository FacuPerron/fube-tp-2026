package com.tiendaonline.pedidos.model;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class PedidoDTORequest {
    
    @NotNull(message = "El id del cliente es obligatorio.")
    private UUID idCliente;
    
    @NotNull(message = "El pedido debe contener al menos un item")
    @Valid
    private List<ItemPedidoDTORequest> items;
    
}
