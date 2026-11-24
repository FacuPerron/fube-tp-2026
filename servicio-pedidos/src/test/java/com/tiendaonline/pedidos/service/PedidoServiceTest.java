package com.tiendaonline.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.pedidos.client.ClienteClient;
import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.ItemPedido;
import com.tiendaonline.pedidos.model.ItemPedidoDTORequest;
import com.tiendaonline.pedidos.model.Pedido;
import com.tiendaonline.pedidos.model.PedidoDTORequest;
import com.tiendaonline.pedidos.model.PedidoDTOResponse;
import com.tiendaonline.pedidos.repository.ItemPedidoRepository;
import com.tiendaonline.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void crearPedido_DeberiaCrearPedidoYDevolverPedidoDTOResponse() {
        //Test para verificar que el método crearPedido funciona correctamente y devuelve un PedidoDTOResponse con los datos esperados.

        //Simulamos la request que nos llega al servicio, con un idCliente y dos ítems.
        UUID idCliente = UUID.randomUUID();
        UUID idProducto1 = UUID.randomUUID();
        UUID idProducto2 = UUID.randomUUID();

        ItemPedidoDTORequest itemReq1 = new ItemPedidoDTORequest(idProducto1, 2, new BigDecimal("100.00"));
        ItemPedidoDTORequest itemReq2 = new ItemPedidoDTORequest(idProducto2, 1, new BigDecimal("50.00"));
        PedidoDTORequest pedidoRequest = new PedidoDTORequest(idCliente, List.of(itemReq1, itemReq2));

        UUID idPedidoSimulado = UUID.randomUUID();
        UUID idReserva1 = UUID.randomUUID();
        UUID idReserva2 = UUID.randomUUID();
        LocalDateTime fechaSimulada = LocalDateTime.now();
        BigDecimal montoEsperado = new BigDecimal("250.00");

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setIdPedido(idPedidoSimulado);
        pedidoGuardado.setIdCliente(idCliente);
        pedidoGuardado.setEstado(EstadoPedido.CREADO);
        pedidoGuardado.setMontoTotal(montoEsperado);
        pedidoGuardado.setFechaCreacion(fechaSimulada);
        pedidoGuardado.setFechaActualizacion(fechaSimulada);

        ItemPedido itemGuardado1 = new ItemPedido(UUID.randomUUID(), idPedidoSimulado, idProducto1, 2, new BigDecimal("100.00"));
        ItemPedido itemGuardado2 = new ItemPedido(UUID.randomUUID(), idPedidoSimulado, idProducto2, 1, new BigDecimal("50.00"));
        List<ItemPedido> itemsGuardados = List.of(itemGuardado1, itemGuardado2);

        //Simulamos que el cliente existe y que el servicio de inventario reserva el stock de ambos productos.
        doNothing().when(clienteClient).verificarClienteExiste(idCliente);
        when(inventarioClient.reservarStock(eq(idProducto1), any(UUID.class), eq(2))).thenReturn(idReserva1);
        when(inventarioClient.reservarStock(eq(idProducto2), any(UUID.class), eq(1))).thenReturn(idReserva2);

        //Cuando los repos guarden, le decimos que nos devuelvan el pedido y los ítems que simulamos.
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);
        when(itemPedidoRepository.saveAll(anyList())).thenReturn(itemsGuardados);

        //Ejecutamos el método que queremos probar.
        PedidoDTOResponse pedidoResponse = pedidoService.crearPedido(pedidoRequest);

        //Verificamos que la respuesta no sea nula y que los datos sean los esperados.
        assertNotNull(pedidoResponse);
        assertEquals(idPedidoSimulado, pedidoResponse.getIdPedido());
        assertEquals(idCliente, pedidoResponse.getIdCliente());
        assertEquals(EstadoPedido.CREADO, pedidoResponse.getEstado());
        assertEquals(montoEsperado, pedidoResponse.getMontoTotal());
        assertEquals(fechaSimulada, pedidoResponse.getFechaCreacion());
        assertEquals(fechaSimulada, pedidoResponse.getFechaActualizacion());
        assertEquals(2, pedidoResponse.getItems().size());

        //Verificamos si los clientes HTTP y los repos fueron llamados las veces esperadas.
        verify(clienteClient, times(1)).verificarClienteExiste(idCliente);
        verify(inventarioClient, times(1)).reservarStock(eq(idProducto1), any(UUID.class), eq(2));
        verify(inventarioClient, times(1)).reservarStock(eq(idProducto2), any(UUID.class), eq(1));
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(itemPedidoRepository, times(1)).saveAll(anyList());
    }

    @Test
    void crearPedido_CuandoClienteNoExiste_DeberiaLanzarExcepcion() {
        //Test para verificar que el método crearPedido lanza una excepción si el cliente no existe en servicio-clientes.

        //Simulamos la request con un cliente que no existe.
        UUID idCliente = UUID.randomUUID();
        ItemPedidoDTORequest itemReq = new ItemPedidoDTORequest(UUID.randomUUID(), 1, new BigDecimal("100.00"));
        PedidoDTORequest pedidoRequest = new PedidoDTORequest(idCliente, List.of(itemReq));

        //Simulamos que clienteClient lanza NoSuchElementException.
        doThrow(new NoSuchElementException("Cliente no encontrado con ID: " + idCliente))
                .when(clienteClient).verificarClienteExiste(idCliente);

        //Verificamos que se lance la excepción con el mensaje esperado.
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            pedidoService.crearPedido(pedidoRequest);
        });

        assertEquals("Cliente no encontrado con ID: " + idCliente, exception.getMessage());

        //Verificamos que no se haya reservado stock ni guardado nada en la base de datos.
        verify(inventarioClient, times(0)).reservarStock(any(), any(), any());
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
        verify(itemPedidoRepository, times(0)).saveAll(anyList());
    }

    @Test
    void crearPedido_CuandoFallaStockEnSegundoItem_DeberiaLiberarReservasPreviasYLanzarExcepcion() {
        //Test para verificar que si falla la reserva de un ítem, se liberan las reservas anteriores y no se crea el pedido.

        //Simulamos la request con dos ítems.
        UUID idCliente = UUID.randomUUID();
        UUID idProducto1 = UUID.randomUUID();
        UUID idProducto2 = UUID.randomUUID();
        UUID idReserva1 = UUID.randomUUID();

        ItemPedidoDTORequest itemReq1 = new ItemPedidoDTORequest(idProducto1, 2, new BigDecimal("100.00"));
        ItemPedidoDTORequest itemReq2 = new ItemPedidoDTORequest(idProducto2, 5, new BigDecimal("50.00"));
        PedidoDTORequest pedidoRequest = new PedidoDTORequest(idCliente, List.of(itemReq1, itemReq2));

        //Simulamos que el cliente existe, el primer producto se reserva bien, pero el segundo falla por falta de stock.
        doNothing().when(clienteClient).verificarClienteExiste(idCliente);
        when(inventarioClient.reservarStock(eq(idProducto1), any(UUID.class), eq(2))).thenReturn(idReserva1);
        when(inventarioClient.reservarStock(eq(idProducto2), any(UUID.class), eq(5)))
                .thenThrow(new IllegalStateException("No hay stock suficiente para el producto con ID: " + idProducto2));

        //Verificamos que se lance la excepción con el mensaje esperado.
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            pedidoService.crearPedido(pedidoRequest);
        });

        assertEquals("No hay stock suficiente para el producto con ID: " + idProducto2, exception.getMessage());

        //Verificamos que se haya liberado la reserva del primer producto y que no se haya guardado el pedido.
        verify(inventarioClient, times(1)).liberarReserva(idReserva1);
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
        verify(itemPedidoRepository, times(0)).saveAll(anyList());
    }

    @Test
    void cancelarPedido_CuandoEstaCreado_DeberiaCancelarPedidoYLiberarReservas() {
        //Test para verificar que el método cancelarPedido funciona correctamente cuando el pedido está en estado CREADO.

        //Simulamos un pedido existente en estado CREADO y sus ítems.
        UUID idPedido = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        LocalDateTime fechaCreacion = LocalDateTime.now().minusHours(2);

        Pedido pedidoEnBDD = new Pedido();
        pedidoEnBDD.setIdPedido(idPedido);
        pedidoEnBDD.setIdCliente(idCliente);
        pedidoEnBDD.setEstado(EstadoPedido.CREADO);
        pedidoEnBDD.setMontoTotal(new BigDecimal("200.00"));
        pedidoEnBDD.setFechaCreacion(fechaCreacion);
        pedidoEnBDD.setFechaActualizacion(fechaCreacion);

        ItemPedido itemEnBDD = new ItemPedido(UUID.randomUUID(), idPedido, UUID.randomUUID(), 2, new BigDecimal("100.00"));
        List<ItemPedido> itemsEnBDD = List.of(itemEnBDD);

        //Cuando el repo busque y guarde, simulamos los retornos.
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoEnBDD));
        doNothing().when(inventarioClient).liberarReservasPorPedido(idPedido);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoRepository.findByIdPedido(idPedido)).thenReturn(itemsEnBDD);

        //Ejecutamos el método que queremos probar.
        PedidoDTOResponse pedidoResponse = pedidoService.cancelarPedido(idPedido);

        //Verificamos que el estado haya pasado a CANCELADO y los datos sean los esperados.
        assertNotNull(pedidoResponse);
        assertEquals(idPedido, pedidoResponse.getIdPedido());
        assertEquals(EstadoPedido.CANCELADO, pedidoResponse.getEstado());
        assertEquals(1, pedidoResponse.getItems().size());

        //Verificamos que se hayan liberado las reservas y guardado el pedido actualizado.
        verify(pedidoRepository, times(1)).findById(idPedido);
        verify(inventarioClient, times(1)).liberarReservasPorPedido(idPedido);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(itemPedidoRepository, times(1)).findByIdPedido(idPedido);
    }

    @Test
    void cancelarPedido_CuandoEstaConfirmado_DeberiaCancelarPedidoYLiberarReservas() {
        //Test para verificar que el método cancelarPedido funciona correctamente cuando el pedido está en estado CONFIRMADO.

        //Simulamos un pedido existente en estado CONFIRMADO.
        UUID idPedido = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        LocalDateTime fechaCreacion = LocalDateTime.now().minusHours(1);

        Pedido pedidoEnBDD = new Pedido();
        pedidoEnBDD.setIdPedido(idPedido);
        pedidoEnBDD.setIdCliente(idCliente);
        pedidoEnBDD.setEstado(EstadoPedido.CONFIRMADO);
        pedidoEnBDD.setMontoTotal(new BigDecimal("150.00"));
        pedidoEnBDD.setFechaCreacion(fechaCreacion);
        pedidoEnBDD.setFechaActualizacion(fechaCreacion);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoEnBDD));
        doNothing().when(inventarioClient).liberarReservasPorPedido(idPedido);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemPedidoRepository.findByIdPedido(idPedido)).thenReturn(List.of());

        //Ejecutamos el método que queremos probar.
        PedidoDTOResponse pedidoResponse = pedidoService.cancelarPedido(idPedido);

        //Verificamos que el estado haya pasado a CANCELADO.
        assertNotNull(pedidoResponse);
        assertEquals(EstadoPedido.CANCELADO, pedidoResponse.getEstado());

        verify(inventarioClient, times(1)).liberarReservasPorPedido(idPedido);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void cancelarPedido_CuandoNoExiste_DeberiaLanzarExcepcion() {
        //Test para verificar que el método cancelarPedido lanza una excepción si el pedido no existe en la base de datos.

        //Simulamos un ID de pedido que no existe.
        UUID idPedido = UUID.randomUUID();

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            pedidoService.cancelarPedido(idPedido);
        });

        assertEquals("Pedido no encontrado con ID: " + idPedido, exception.getMessage());

        //Verificamos que no se liberen reservas ni se guarde nada.
        verify(inventarioClient, times(0)).liberarReservasPorPedido(any());
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
    }

    @Test
    void cancelarPedido_CuandoEstadoNoEsValido_DeberiaLanzarExcepcion() {
        //Test para verificar que el método cancelarPedido lanza una excepción si el pedido ya está CANCELADO o EXPIRADO.

        //Simulamos un pedido en estado EXPIRADO.
        UUID idPedido = UUID.randomUUID();

        Pedido pedidoEnBDD = new Pedido();
        pedidoEnBDD.setIdPedido(idPedido);
        pedidoEnBDD.setEstado(EstadoPedido.EXPIRADO);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoEnBDD));

        //Verificamos que se lance la excepción con el mensaje esperado.
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            pedidoService.cancelarPedido(idPedido);
        });

        assertEquals("Solo se pueden cancelar pedidos en estado CREADO o CONFIRMADO", exception.getMessage());

        //Verificamos que no se liberen reservas ni se guarde el pedido.
        verify(inventarioClient, times(0)).liberarReservasPorPedido(any());
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
    }

    @Test
    void obtenerPedidoPorId_DeberiaDevolverPedidoDTOResponse() {
        //Test para verificar que el método obtenerPedidoPorId funciona correctamente y devuelve un PedidoDTOResponse con los datos esperados.

        //Simulamos y seteamos todos los datos del Pedido y sus ítems.
        UUID idPedido = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();
        UUID idItem = UUID.randomUUID();
        UUID idProducto = UUID.randomUUID();
        LocalDateTime fechaCreacion = LocalDateTime.now();

        Pedido pedidoEnBDD = new Pedido();
        pedidoEnBDD.setIdPedido(idPedido);
        pedidoEnBDD.setIdCliente(idCliente);
        pedidoEnBDD.setEstado(EstadoPedido.CREADO);
        pedidoEnBDD.setMontoTotal(new BigDecimal("300.00"));
        pedidoEnBDD.setFechaCreacion(fechaCreacion);
        pedidoEnBDD.setFechaActualizacion(fechaCreacion);

        ItemPedido itemEnBDD = new ItemPedido(idItem, idPedido, idProducto, 3, new BigDecimal("100.00"));
        List<ItemPedido> itemsEnBDD = List.of(itemEnBDD);

        //Cuando los repos busquen por ID, le decimos que nos devuelvan lo que simulamos.
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoEnBDD));
        when(itemPedidoRepository.findByIdPedido(idPedido)).thenReturn(itemsEnBDD);

        //Ejecutamos el método que queremos probar.
        PedidoDTOResponse pedidoResponse = pedidoService.obtenerPedidoPorId(idPedido);

        //Verificamos que la respuesta no sea nula y que los datos sean los esperados.
        assertNotNull(pedidoResponse);
        assertEquals(idPedido, pedidoResponse.getIdPedido());
        assertEquals(idCliente, pedidoResponse.getIdCliente());
        assertEquals(EstadoPedido.CREADO, pedidoResponse.getEstado());
        assertEquals(new BigDecimal("300.00"), pedidoResponse.getMontoTotal());
        assertEquals(fechaCreacion, pedidoResponse.getFechaCreacion());
        assertEquals(1, pedidoResponse.getItems().size());
        assertEquals(idItem, pedidoResponse.getItems().get(0).getIdItemPedido());

        //Verificamos si los métodos de los repos fueron llamados una vez.
        verify(pedidoRepository, times(1)).findById(idPedido);
        verify(itemPedidoRepository, times(1)).findByIdPedido(idPedido);
    }

    @Test
    void obtenerPedidoPorId_CuandoNoExiste_DeberiaLanzarExcepcion() {
        //Test para verificar que el método obtenerPedidoPorId lanza una excepción si el pedido no existe en la base de datos.

        //Simulamos un ID de pedido que no existe en la base de datos.
        UUID idPedido = UUID.randomUUID();

        //Simulamos que el repo nos dice que no encontró el pedido con ese ID.
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            pedidoService.obtenerPedidoPorId(idPedido);
        });

        assertEquals("Pedido no encontrado con ID: " + idPedido, exception.getMessage());

        //Verificamos que findById haya sido llamado una vez y que no se hayan buscado ítems.
        verify(pedidoRepository, times(1)).findById(idPedido);
        verify(itemPedidoRepository, times(0)).findByIdPedido(any());
    }

    @Test
    void listarPedidosPorCliente_DeberiaDevolverListaDePedidoDTOResponse() {
        
        //Test para verificar que el método listarPedidosPorCliente devuelve la lista de pedidos de un cliente.

        //Simulamos un cliente con un pedido y un ítem asociado.
        UUID idCliente = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        LocalDateTime fechaCreacion = LocalDateTime.now();

        Pedido pedidoEnBDD = new Pedido();
        pedidoEnBDD.setIdPedido(idPedido);
        pedidoEnBDD.setIdCliente(idCliente);
        pedidoEnBDD.setEstado(EstadoPedido.CREADO);
        pedidoEnBDD.setMontoTotal(new BigDecimal("100.00"));
        pedidoEnBDD.setFechaCreacion(fechaCreacion);
        pedidoEnBDD.setFechaActualizacion(fechaCreacion);

        ItemPedido itemEnBDD = new ItemPedido(UUID.randomUUID(), idPedido, UUID.randomUUID(), 1, new BigDecimal("100.00"));

        when(pedidoRepository.findByIdCliente(idCliente)).thenReturn(List.of(pedidoEnBDD));
        when(itemPedidoRepository.findByIdPedido(idPedido)).thenReturn(List.of(itemEnBDD));

        //Ejecutamos el método que queremos probar.
        List<PedidoDTOResponse> pedidosResponse = pedidoService.listarPedidosPorCliente(idCliente);

        //Verificamos que la lista no sea nula y contenga los datos esperados.
        assertNotNull(pedidosResponse);
        assertEquals(1, pedidosResponse.size());
        assertEquals(idPedido, pedidosResponse.get(0).getIdPedido());
        assertEquals(idCliente, pedidosResponse.get(0).getIdCliente());
        assertEquals(1, pedidosResponse.get(0).getItems().size());

        //Verificamos si los métodos de los repos fueron llamados una vez.
        verify(pedidoRepository, times(1)).findByIdCliente(idCliente);
        verify(itemPedidoRepository, times(1)).findByIdPedido(idPedido);
    }

}
