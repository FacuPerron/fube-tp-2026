package com.tiendaonline.inventario.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID idProducto;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku; //Código único del producto (Stock Keeping Unit)

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;
}
