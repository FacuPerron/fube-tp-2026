package com.tiendaonline.inventario.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor
public class ProductoDTORequest {
    private String nombre;
    private String sku;
    private Integer cantidadDisponible;
}
