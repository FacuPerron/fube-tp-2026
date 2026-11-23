package com.tiendaonline.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.inventario.model.Producto;
import com.tiendaonline.inventario.model.ProductoDTORequest;
import com.tiendaonline.inventario.model.ProductoDTOResponse;
import com.tiendaonline.inventario.model.Reserva;
import com.tiendaonline.inventario.model.enume.EstadoReserva;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository inventarioRepository;
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    void registrarProducto_DeberiaRegistrarProductoYDevolverProductoRegistrado() {
        //Test para verificar que el método registrarProducto funciona correctamente y devuelve un ProductoRegistrado con los datos esperados.

        //Simulamos la request que nos llega al servicio, con un nombre, monto y stock inicial.
        ProductoDTORequest productoRequest = new ProductoDTORequest("Producto A", "SKU-001" , 10);

        UUID idProducto = UUID.randomUUID();

        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombre(productoRequest.getNombre());
        producto.setSku(productoRequest.getSku());
        producto.setCantidadDisponible(productoRequest.getCantidadDisponible());

        //Cuando el repo guarde, le decimos que nos devuelva el producto que simulamos.
        when(inventarioRepository.save(any(Producto.class))).thenReturn(producto);

        //Ejecutamos el método que queremos probar.
        ProductoDTOResponse productoResponse = inventarioService.registrarProducto(productoRequest);

        //Verificamos que la respuesta no sea nula y que los datos sean los esperados.
        assertNotNull(productoResponse);
        assertEquals(idProducto, productoResponse.getIdProducto());
        assertEquals(productoRequest.getNombre(), productoResponse.getNombre());
        assertEquals(productoRequest.getDescripcion(), productoResponse.getDescripcion());
        assertEquals(productoRequest.getStock(), productoResponse.getStock());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Producto.
        verify(inventarioRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void reservarStock_DeberiaReservarStockYActualizarCantidadDisponible() {
        //Test para verificar que el método reservarStock funciona correctamente y actualiza la cantidad disponible del producto.

        UUID idProducto = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        Integer cantidadDisponible = 10;
        Integer cantidadAReservar = 5;

        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombre("Producto A");
        producto.setSku("SKU-001");
        producto.setCantidadDisponible(cantidadDisponible);

        //Simulamos que el repo encuentra el producto por su ID.
        when(inventarioRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        // Simulamos que el repo de reservas devuelve la misma reserva que se intenta guardar
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Ejecutamos el método que queremos probar.
        inventarioService.reservarStock(idProducto, idPedido ,cantidadAReservar);

        //Verificamos que la cantidad disponible del producto se haya actualizado correctamente.
        assertEquals(cantidadDisponible - cantidadAReservar, producto.getCantidadDisponible());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Producto.
        verify(inventarioRepository, times(1)).save(any(Producto.class));
    
        // Verificamos que se haya creado y guardado la reserva
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void reservarStock_DeberiaLanzarExcepcionSiNoHayStockSuficiente() {
        //Test para verificar que el método reservarStock lanza una excepción si no hay stock suficiente.

        UUID idProducto = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        Integer cantidadDisponible = 3;
        Integer cantidadAReservar = 5;

        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombre("Producto A");
        producto.setSku("SKU-001");
        producto.setCantidadDisponible(cantidadDisponible);

        //Simulamos que el repo encuentra el producto por su ID.
        when(inventarioRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        //Ejecutamos el método que queremos probar y verificamos que lance la excepción esperada.
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.reservarStock(idProducto, idPedido ,cantidadAReservar);
        });


        assertEquals("No hay stock suficiente para reservar", exception.getMessage());

        //Verificamos que la cantidad disponible del producto no haya cambiado.
        assertEquals(cantidadDisponible, producto.getCantidadDisponible());

        //Verificamos que el método save del repo no haya sido llamado.
        verify(inventarioRepository, times(0)).save(any(Producto.class));
    }

    @Test 
    void consultarStockDisponible_DeberiaDevolverCantidadDisponible() {
        //Test para verificar que el método consultarStockDisponible funciona correctamente y devuelve la cantidad disponible del producto.

        UUID idProducto = UUID.randomUUID();
        Integer cantidadDisponible = 10;

        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombre("Producto A");
        producto.setSku("SKU-001");
        producto.setCantidadDisponible(cantidadDisponible);

        //Simulamos que el repo encuentra el producto por su ID.
        when(inventarioRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        //Ejecutamos el método que queremos probar.
        Integer cantidadDevuelta = inventarioService.consultarStockDisponible(idProducto);

        //Verificamos que la cantidad devuelta sea la esperada.
        assertEquals(cantidadDisponible, cantidadDevuelta);

        //Verificamos que el método findById del repo haya sido llamado una vez con el ID del producto.
        verify(inventarioRepository, times(1)).findById(idProducto);
    }

    @Test
    void consultarStockDisponible_CuandoNoExisteProducto_DeberiaLanzarExcepcion() {
        //Test para verificar que el método consultarStockDisponible lanza una excepción si el producto no existe en la base de datos.

        UUID idProducto = UUID.randomUUID();

        //Simulamos que el repo nos dice que no encontró el producto con ese ID.
        when(inventarioRepository.findById(idProducto)).thenReturn(java.util.Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        java.util.NoSuchElementException exception = org.junit.jupiter.api.Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            inventarioService.consultarStockDisponible(idProducto);
        });
        assertEquals("Producto no encontrado con ID: " + idProducto, exception.getMessage());

        //Verificamos que el método findById del repo haya sido llamado una vez con el ID del producto.
        verify(inventarioRepository, times(1)).findById(idProducto);
    }

    



}
