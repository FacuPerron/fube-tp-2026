package com.tiendaonline.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.tiendaonline.inventario.model.Reserva;
import com.tiendaonline.inventario.model.ReservaDTORequest;
import com.tiendaonline.inventario.model.enume.EstadoReserva;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test 
    void liberarReserva_DeberiaLiberarReservaYActualizarCantidadDisponible() {
        //Test para verificar que el método liberarReserva funciona correctamente y actualiza la cantidad disponible del producto.

        UUID idProducto = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        Integer cantidadDisponible = 10;
        Integer cantidadAReservar = 5;
        EstadoReserva estadoReserva = EstadoReserva.ACTIVA;

        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombre("Producto A");
        producto.setSku("SKU-001");
        producto.setCantidadDisponible(cantidadDisponible);

        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva);
        reserva.setIdProducto(idProducto);
        reserva.setIdPedido(idPedido);
        reserva.setCantidad(cantidadAReservar);
        reserva.setEstado(estadoReserva);
        reserva.setFechaCreacion(java.time.LocalDateTime.now());

        //Simulamos que el repo encuentra el producto por su ID.
        when(productoRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        // Simulamos que el repo de reservas encuentra la reserva por el ID del pedido y del producto
        when(reservaRepository.findById(idReserva)).thenReturn(java.util.Optional.of(reserva));

        //Ejecutamos el método que queremos probar.
        reservaService.liberarReserva(idReserva);

        //Verificamos que la cantidad disponible del producto se haya actualizado correctamente.
        assertEquals(cantidadDisponible + cantidadAReservar, producto.getCantidadDisponible());

        //Verificamos que el estado de la reserva quede LIBERADO.
        assertEquals(EstadoReserva.LIBERADA, reserva.getEstado());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Producto.
        verify(productoRepository, times(1)).save(any(Producto.class));
    
        // Verificamos que se haya eliminado la reserva
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void liberarReserva_CuandoNoExisteReserva_DeberiaLanzarExcepcion() {
        //Test para verificar que el método liberarReserva lanza una excepción si la reserva no existe en la base de datos.

        UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        
        //Simulamos que el repo de reservas nos dice que no encontró la reserva con ese ID.
        when(reservaRepository.findById(idReserva)).thenReturn(java.util.Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        java.util.NoSuchElementException exception = org.junit.jupiter.api.Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            reservaService.liberarReserva(idReserva);
        });
        assertEquals("Reserva no encontrada con ID de pedido", exception.getMessage());

        //Verificamos que el método findById del repo de reservas haya sido llamado una vez con el ID del pedido.
        verify(reservaRepository, times(1)).findById(idReserva);
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

        ReservaDTORequest request = new ReservaDTORequest();
        request.setCantidad(cantidadAReservar);
        request.setIdPedido(idPedido);
        request.setIdProducto(idProducto);

        //Simulamos que el repo encuentra el producto por su ID.
        when(productoRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        // Simulamos que el repo de reservas devuelve la misma reserva que se intenta guardar
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Ejecutamos el método que queremos probar.
        reservaService.reservarStock(request);

        //Verificamos que la cantidad disponible del producto se haya actualizado correctamente.
        assertEquals(cantidadDisponible - cantidadAReservar, producto.getCantidadDisponible());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Producto.
        verify(productoRepository, times(1)).save(any(Producto.class));
    
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

        ReservaDTORequest request = new ReservaDTORequest();
        request.setCantidad(cantidadAReservar);
        request.setIdPedido(idPedido);
        request.setIdProducto(idProducto);

        //Simulamos que el repo encuentra el producto por su ID.
        when(productoRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        //Ejecutamos el método que queremos probar y verificamos que lance la excepción esperada.
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            reservaService.reservarStock(request);
        });

        assertEquals("No hay stock suficiente para reservar", exception.getMessage());

        //Verificamos que la cantidad disponible del producto no haya cambiado.
        assertEquals(cantidadDisponible, producto.getCantidadDisponible());

        //Verificamos que el método save del repo no haya sido llamado.
        verify(productoRepository, times(0)).save(any(Producto.class));
    }
}
