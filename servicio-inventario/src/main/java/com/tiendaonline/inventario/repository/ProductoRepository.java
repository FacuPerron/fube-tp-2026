package com.tiendaonline.inventario.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tiendaonline.inventario.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    boolean existsBySku(String sku);
    boolean existsByIdProducto(UUID idProducto);
    java.util.Optional<Producto> findBySku(String sku);
}
