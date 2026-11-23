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
import com.tiendaonline.inventario.model.enume.EstadoReserva;
import com.tiendaonline.inventario.repository.ProductoRepository;
import com.tiendaonline.inventario.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private ProductoRepository inventarioRepository;
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private InventarioService inventarioService;

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
        when(inventarioRepository.findById(idProducto)).thenReturn(java.util.Optional.of(producto));

        // Simulamos que el repo de reservas encuentra la reserva por el ID del pedido y del producto
        when(reservaRepository.findById(idReserva)).thenReturn(java.util.Optional.of(reserva));

        //Ejecutamos el método que queremos probar.
        inventarioService.liberarReserva(idReserva, cantidadAReservar);

        //Verificamos que la cantidad disponible del producto se haya actualizado correctamente.
        assertEquals(cantidadDisponible + cantidadAReservar, producto.getCantidadDisponible());

        //Verificamos que el estado de la reserva quede LIBERADO.
        assertEquals(EstadoReserva.LIBERADA, reserva.getEstado());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Producto.
        verify(inventarioRepository, times(1)).save(any(Producto.class));
    
        // Verificamos que se haya eliminado la reserva
        verify(reservaRepository, times(1)).delete(any(Reserva.class));
    }

    @Test
    void liberarReserva_CuandoNoExisteReserva_DeberiaLanzarExcepcion() {
        //Test para verificar que el método liberarReserva lanza una excepción si la reserva no existe en la base de datos.

        UUID idProducto = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        Integer cantidadAReservar = 5;

        //Simulamos que el repo de reservas nos dice que no encontró la reserva con ese ID.
        when(reservaRepository.findById(idPedido)).thenReturn(java.util.Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        java.util.NoSuchElementException exception = org.junit.jupiter.api.Assertions.assertThrows(java.util.NoSuchElementException.class, () -> {
            inventarioService.liberarReserva(idReserva, cantidadAReservar);
        });
        assertEquals("Reserva no encontrada con ID de pedido: " + idPedido, exception.getMessage());

        //Verificamos que el método findById del repo de reservas haya sido llamado una vez con el ID del pedido.
        verify(reservaRepository, times(1)).findById(idPedido);
    }
}
