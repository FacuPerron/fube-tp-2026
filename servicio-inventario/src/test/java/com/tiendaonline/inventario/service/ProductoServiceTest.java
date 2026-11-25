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
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ProductoService productoService;

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
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        //Ejecutamos el método que queremos probar.
        ProductoDTOResponse productoResponse = productoService.registrarProducto(productoRequest);

        //Verificamos que la respuesta no sea nula y que los datos sean los esperados.
        assertNotNull(productoResponse);
        assertEquals(idProducto, productoResponse.getIdProducto());
        assertEquals(productoRequest.getNombre(), productoResponse.getNombre());
        assertEquals(productoRequest.getCantidadDisponible(), productoResponse.getCantidadDisponible());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Producto.
        verify(productoRepository, times(1)).save(any(Producto.class));
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
        when(productoRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        //Ejecutamos el método que queremos probar.
        Integer cantidadDevuelta = productoService.consultarStockDisponible(idProducto);

        //Verificamos que la cantidad devuelta sea la esperada.
        assertEquals(cantidadDisponible, cantidadDevuelta);

        //Verificamos que el método findById del repo haya sido llamado una vez con el ID del producto.
        verify(productoRepository, times(1)).findById(idProducto);
    }

    @Test
    void consultarStockDisponible_CuandoNoExisteProducto_DeberiaLanzarExcepcion() {
        //Test para verificar que el método consultarStockDisponible lanza una excepción si el producto no existe en la base de datos.

        UUID idProducto = UUID.randomUUID();

        //Simulamos que el repo nos dice que no encontró el producto con ese ID.
        when(productoRepository.findById(idProducto)).thenReturn(java.util.Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        java.util.NoSuchElementException exception = org.junit.jupiter.api.Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            productoService.consultarStockDisponible(idProducto);
        });
        assertEquals("Producto no encontrado con ID: " + idProducto, exception.getMessage());

        //Verificamos que el método findById del repo haya sido llamado una vez con el ID del producto.
        verify(productoRepository, times(1)).findById(idProducto);
    }

    



}
