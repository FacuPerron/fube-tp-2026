package com.tiendaonline.inventario.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor 
public class ProductoDTOResponse {
    private UUID idProducto;
    private String nombre;
    private String sku;
    private Integer cantidadDisponible;
}
