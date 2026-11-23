package com.tiendaonline.inventario.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import com.tiendaonline.inventario.model.Producto;
import com.tiendaonline.inventario.model.ProductoDTORequest;
import com.tiendaonline.inventario.model.ProductoDTOResponse;
import com.tiendaonline.inventario.repository.ProductoRepository;

public class ProductoService {

    ProductoRepository productoRepository;
    /* InventarioService debe:
    - Registrar un nuevo producto en el inventario. Listo.
    - Reservar unidades de stock para un pedido, rechazando la operación si no hay stock suficiente. 
    - Actualizar el stock de un producto, asegurando que no se pueda establecer un stock negativo. Listo
    - Liberar una reserva existente.
    - Consultar el stock disponible de un producto. Listo
     */
    

    // Implementación de los métodos del servicio de inventario

    // Método para registrar un nuevo producto en el inventario
    public ProductoDTOResponse registrarProducto(ProductoDTORequest productoRequest) {
        
        if(productoRepository.existsBySku(productoRequest.getSku())) {
            throw new IllegalArgumentException("El SKU ya está registrado");
        }

        //Creamos un nuevo producto a partir del request recibido
        Producto producto = new Producto();
        producto.setNombre(productoRequest.getNombre());
        producto.setSku(productoRequest.getSku());
        producto.setCantidadDisponible(productoRequest.getCantidadDisponible());

        //Guardamos el producto en la base de datos
        producto = productoRepository.save(producto);

        //Creamos la response para devolverla en la respuesta de la API.
        ProductoDTOResponse productoResponse = new ProductoDTOResponse();
        productoResponse.setIdProducto(producto.getIdProducto());
        productoResponse.setNombre(producto.getNombre());
        productoResponse.setSku(producto.getSku());
        productoResponse.setCantidadDisponible(producto.getCantidadDisponible());

        return productoResponse;
    }

    //Metodo para consultar un producto por su ID.
    public ProductoDTOResponse consultarProducto(UUID idProducto){
        //Buscamos el producto por su ID en la base de datos, si no lo encuentra se lanza una excepción.
        Producto producto = productoRepository.findById(idProducto).orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + idProducto));

        ProductoDTOResponse response = new ProductoDTOResponse();
        response.setIdProducto(producto.getIdProducto());
        response.setNombre(producto.getNombre());
        response.setSku(producto.getSku());
        response.setCantidadDisponible(producto.getCantidadDisponible());

        //Retornamos la response.
        return response;
    }

    //Metodo para consultar disponibilidad de un producto.
    public Integer consultarStockDisponible(UUID idProducto){
        //Buscamos el producto por su ID en la base de datos, si no lo encuentra se lanza una excepción.
        Producto producto = productoRepository.findById(idProducto).orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + idProducto));


        //Retornamos la disponibilidad de un producto.
        return producto.getCantidadDisponible();
    }

    //Metodo para actualizar el stock de un producto.
    public ProductoDTOResponse actualizarStock(UUID idProducto, Integer nuevaCantidad) {
        //Buscamos el producto por su ID en la base de datos.
        Producto producto = this.buscarProductoPorId(idProducto);

        //Validamos que la nueva cantidad no sea negativa.
        if(nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad de stock no puede ser negativa");
        }

        //Actualizamos la cantidad disponible del producto.
        producto.setCantidadDisponible(nuevaCantidad);

        //Guardamos los cambios en la base de datos.
        productoRepository.save(producto);

        return new ProductoDTOResponse(idProducto, producto.getNombre(), producto.getSku(), producto.getCantidadDisponible());
    }


    public Producto buscarProductoPorId(UUID idProducto) {
        //Buscamos el producto por su ID en la base de datos, si no lo encuentra se lanza una excepción.
        return productoRepository.findById(idProducto).orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + idProducto));
    }
    public Producto buscarProductoPorSku(String sku) {
        //Buscamos el producto por su SKU en la base de datos, si no lo encuentra se lanza una excepción.
        return productoRepository.findBySku(sku).orElseThrow(() -> new NoSuchElementException("Producto no encontrado con SKU: " + sku));
    }

}
