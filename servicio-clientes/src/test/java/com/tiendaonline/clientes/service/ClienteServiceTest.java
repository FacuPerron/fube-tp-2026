package com.tiendaonline.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.tiendaonline.clientes.client.PedidoClient;
import com.tiendaonline.clientes.client.PedidoClient.ItemPedidoResponse;
import com.tiendaonline.clientes.client.PedidoClient.PedidoResponse;
import com.tiendaonline.clientes.model.Cliente;
import com.tiendaonline.clientes.model.ClienteDTORequest;
import com.tiendaonline.clientes.model.ClienteDTOResponse;
import com.tiendaonline.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PedidoClient pedidoClient;
    @Mock
    private FeatureFlagService featureFlagService;

    @InjectMocks 
    private ClienteService clienteService;

    @Test
    void registrarCliente_DeberiaRegistrarClienteYDevolverClienteDTOResponse() {
        //Test para verificar que el método registrarCliente funciona correctamente y devuelve un ClienteDTOResponse con los datos esperados.
        
        //Simulamos la request que nos llega al servicio, con un nombre y un email.
        ClienteDTORequest clienteRequest = new ClienteDTORequest("Juan Perez", "juan.perez@example.com");

        UUID idCliente = UUID.randomUUID();
        LocalDateTime fechaALtaSimulada = LocalDateTime.now();

        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setNombre(clienteRequest.getNombre());
        cliente.setEmail(clienteRequest.getEmail());
        cliente.setFechaAlta(fechaALtaSimulada);

        //Cuando el repo guarde, le decimos que nos devuelva el cliente que simulamos.
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        //Ejecutamos el método que queremos probar.
        ClienteDTOResponse clienteResponse = clienteService.registrarCliente(clienteRequest);

        //Verificamos que la respuesta no sea nula y que los datos sean los esperados.
        assertNotNull(clienteResponse);
        assertEquals(idCliente, clienteResponse.getIdCliente());
        assertEquals(clienteRequest.getNombre(), clienteResponse.getNombre());
        assertEquals(clienteRequest.getEmail(), clienteResponse.getEmail());
        assertEquals(fechaALtaSimulada, clienteResponse.getFechaAlta());

        //Verificamos si el método save del repo fue llamado una vez con cualquier objeto de tipo Cliente.
        verify(clienteRepository, times(1)).save(any(Cliente.class));

    }

    @Test
    void registrarCliente_DeberiaLanzarExcepcionSiMailEsRepetido() {
        //Test para verificar que el método registrarCliente lanza una excepción si el email ya está registrado en la base de datos.
        
        //Simulamos la request que nos llega al servicio, con un nombre y un email que ya está registrado en la base de datos.
        ClienteDTORequest clienteRequest = new ClienteDTORequest("Juan Perez", "juan.perez@example.com");
        
        //Simulamos que el repo nos dice que el email ya existe en la base de datos.
        when(clienteRepository.existsByEmail(clienteRequest.getEmail())).thenReturn(true);

        //Verificamos que se lance la excepción con el mensaje esperado.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.registrarCliente(clienteRequest);
        });

        assertEquals("El email ya está registrado", exception.getMessage());

        //Verificamos que el método save del repo no haya sido llamado.
        verify(clienteRepository, times(0)).save(any(Cliente.class));
    }

    @Test
    void registrarCliente_DeberiaLanzarExcepcionSiElCampoNombreEsNulo() {
        //Test para verificar que el método registrarCliente lanza una excepción si el campo nombre es nulo.
        
        //Simulamos la request que nos llega al servicio, con un nombre nulo y un email válido.
        ClienteDTORequest clienteRequest = new ClienteDTORequest(null, "juan.perez@example.com");

        //Verificamos que se lance la excepción con el mensaje esperado.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.registrarCliente(clienteRequest);
        });

        assertEquals("El nombre y el email son obligatorios", exception.getMessage());

        //Verificamos que el método save del repo no haya sido llamado.
        verify(clienteRepository, times(0)).save(any(Cliente.class));
    }

    @Test
    void registrarCliente_DeberiaLanzarExcepcionSiElCampoMailEsNulo() {
        //Test para verificar que el método registrarCliente lanza una excepción si el campo email es nulo.
        //Hace lo mismo que el de arriba pero con el campo email nulo.

        ClienteDTORequest clienteRequest = new ClienteDTORequest("Juan Perez", null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.registrarCliente(clienteRequest);
        });

        assertEquals("El nombre y el email son obligatorios", exception.getMessage());

        verify(clienteRepository, times(0)).save(any(Cliente.class));
    }

    
    @Test
    void obtenerClientePorId_DeberiaDevolverClienteDTOResponse() {
        //Test para verificar que el método obtenerClientePorId funciona correctamente y devuelve un ClienteDTOResponse con los datos esperados.
        
        //Simulamos y seteamos todos los datos del Cliente.
        UUID idCliente = UUID.randomUUID();
        LocalDateTime fechaALta = LocalDateTime.now();

        Cliente clienteEnBDD = new Cliente();

        clienteEnBDD.setIdCliente(idCliente);
        clienteEnBDD.setNombre("Juan Perez");
        clienteEnBDD.setEmail("juan.perez@example.com");
        clienteEnBDD.setFechaAlta(fechaALta);

        //Cuando el repo busque por ID, le decimos que nos devuelva el cliente que simulamos.
        when(clienteRepository.findById(idCliente)).thenReturn(java.util.Optional.of(clienteEnBDD));

        //Ejecutamos el método que queremos probar.
        ClienteDTOResponse clienteResponse = clienteService.obtenerClientePorId(idCliente);

        //Verificamos que la respuesta no sea nula y que los datos sean los esperados.
        assertNotNull(clienteResponse);
        assertEquals(idCliente, clienteResponse.getIdCliente());
        assertEquals("Juan Perez", clienteResponse.getNombre());
        assertEquals("juan.perez@example.com", clienteResponse.getEmail());
        assertEquals(fechaALta, clienteResponse.getFechaAlta());
        
        //Verificamos si el método findById del repo fue llamado una vez con el ID del cliente.
        verify(clienteRepository, times(1)).findById(idCliente);

    }


    @Test
    void obtenerClientePorId_CuandoNoExiste_DeberiaLanzarExcepcion() {
        //Test para verificar que el método obtenerClientePorId lanza una excepción si el cliente no existe en la base de datos.

        //Simulamos un ID de cliente que no existe en la base de datos.
        UUID idCliente = UUID.randomUUID();
        
        //Simulamos que el repo nos dice que no encontró el cliente con ese ID.
        when(clienteRepository.findById(idCliente)).thenReturn(java.util.Optional.empty());

        //Verificamos que se lance la excepción con el mensaje esperado.
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            clienteService.obtenerClientePorId(idCliente);
        });
        assertEquals("Cliente no encontrado con ID: " + idCliente, exception.getMessage());

        //Verificamos que el método findById del repo haya sido llamado una vez con el ID del cliente.
        verify(clienteRepository, times(1)).findById(idCliente);

    }



    @Test
    void obtenerPedidosDeCliente_DeberiaDevolverListaDePedidos() {
        //Test para verificar que el método obtenerPedidosDeCliente devuelve la lista de pedidos de un cliente existente.

        //Simulamos un cliente existente y la respuesta que nos devuelve servicio-pedidos.
        UUID idCliente = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        LocalDateTime fechaSimulada = LocalDateTime.now();

        ItemPedidoResponse itemSimulado = new ItemPedidoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2,
                new BigDecimal("100.00")
        );

        PedidoResponse pedidoSimulado = new PedidoResponse(
                idPedido,
                idCliente,
                "CREADO",
                new BigDecimal("200.00"),
                fechaSimulada,
                fechaSimulada,
                List.of(itemSimulado)
        );

        //Simulamos que el cliente existe en la BD y que pedidoClient devuelve su lista de pedidos.
        when(clienteRepository.existsById(idCliente)).thenReturn(true);
        when(pedidoClient.obtenerPedidosPorCliente(idCliente)).thenReturn(List.of(pedidoSimulado));

        //Ejecutamos el método que queremos probar.
        List<PedidoResponse> pedidosResponse = clienteService.obtenerPedidosPorCliente(idCliente);

        //Verificamos que la respuesta no sea nula y contenga los datos esperados.
        assertNotNull(pedidosResponse);
        assertEquals(1, pedidosResponse.size());
        assertEquals(idPedido, pedidosResponse.get(0).idPedido());
        assertEquals(idCliente, pedidosResponse.get(0).idCliente());
        assertEquals("CREADO", pedidosResponse.get(0).estado());
        assertEquals(new BigDecimal("200.00"), pedidosResponse.get(0).montoTotal());
        assertEquals(1, pedidosResponse.get(0).items().size());

        //Verificamos si el repo y el cliente HTTP fueron llamados una vez.
        verify(clienteRepository, times(1)).existsById(idCliente);
        verify(pedidoClient, times(1)).obtenerPedidosPorCliente(idCliente);
    }

    @Test
    void obtenerPedidosDeCliente_CuandoClienteNoTienePedidos_DeberiaDevolverListaVacia() {
        //Test para verificar que si el cliente existe pero no tiene pedidos, devuelve una lista vacía.

        UUID idCliente = UUID.randomUUID();

        when(clienteRepository.existsById(idCliente)).thenReturn(true);
        when(pedidoClient.obtenerPedidosPorCliente(idCliente)).thenReturn(List.of());

        List<PedidoResponse> pedidosResponse = clienteService.obtenerPedidosPorCliente(idCliente);

        assertNotNull(pedidosResponse);
        assertEquals(0, pedidosResponse.size());

        verify(clienteRepository, times(1)).existsById(idCliente);
        verify(pedidoClient, times(1)).obtenerPedidosPorCliente(idCliente);
    }

    @Test
    void obtenerPedidosDeCliente_CuandoClienteNoExiste_DeberiaLanzarExcepcion() {
        //Test para verificar que si el cliente no existe en la base de datos, lanza excepción y no llama a servicio-pedidos.

        UUID idCliente = UUID.randomUUID();

        //Simulamos que el cliente no existe en la BD.
        when(clienteRepository.existsById(idCliente)).thenReturn(false);

        //Verificamos que se lance la excepción con el mensaje esperado.
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            clienteService.obtenerPedidosPorCliente(idCliente);
        });

        assertEquals("Cliente no encontrado con ID: " + idCliente, exception.getMessage());

        //Verificamos que no se haya llamado a servicio-pedidos.
        verify(clienteRepository, times(1)).existsById(idCliente);
        verify(pedidoClient, times(0)).obtenerPedidosPorCliente(any());
    }


    @Test
    void obtenerClientePorId_CuandoFlagMaskPiiEstaActiva_DeberiaDevolverDatosEnmascarados() {
        //Test para verificar que si la flag mask-pii está activa, se devuelven el nombre y email con asteriscos.

        UUID idCliente = UUID.randomUUID();
        LocalDateTime fechaAlta = LocalDateTime.now();

        Cliente clienteEnBDD = new Cliente();
        clienteEnBDD.setIdCliente(idCliente);
        clienteEnBDD.setNombre("Juan Perez");
        clienteEnBDD.setEmail("juan.perez@example.com");
        clienteEnBDD.setFechaAlta(fechaAlta);

        //Simulamos que el cliente existe en la BD y que FeatureFlagService devuelve true para la flag.
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteEnBDD));
        when(featureFlagService.isMaskPiiActivo(idCliente.toString())).thenReturn(true);

        //Ejecutamos el método a probar.
        ClienteDTOResponse clienteResponse = clienteService.obtenerClientePorId(idCliente);

        //Verificamos que el nombre y el email vengan enmascarados con asteriscos.
        assertNotNull(clienteResponse);
        assertEquals(idCliente, clienteResponse.getIdCliente());
        assertEquals("J*** P***", clienteResponse.getNombre());
        assertEquals("****@****.com", clienteResponse.getEmail());
        assertEquals(fechaAlta, clienteResponse.getFechaAlta());

        verify(clienteRepository, times(1)).findById(idCliente);
        verify(featureFlagService, times(1)).isMaskPiiActivo(idCliente.toString());
    }

    @Test
    void obtenerClientePorId_CuandoFlagMaskPiiEstaInactiva_DeberiaDevolverDatosNormales() {
        //Test para verificar que si la flag mask-pii está inactiva, se devuelven los datos reales sin enmascarar.

        UUID idCliente = UUID.randomUUID();
        LocalDateTime fechaAlta = LocalDateTime.now();

        Cliente clienteEnBDD = new Cliente();
        clienteEnBDD.setIdCliente(idCliente);
        clienteEnBDD.setNombre("Juan Perez");
        clienteEnBDD.setEmail("juan.perez@example.com");
        clienteEnBDD.setFechaAlta(fechaAlta);

        //Simulamos que el cliente existe en la BD y que FeatureFlagService devuelve false para la flag.
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteEnBDD));
        when(featureFlagService.isMaskPiiActivo(idCliente.toString())).thenReturn(false);

        //Ejecutamos el método a probar.
        ClienteDTOResponse clienteResponse = clienteService.obtenerClientePorId(idCliente);

        //Verificamos que los datos se devuelvan normales, sin asteriscos.
        assertNotNull(clienteResponse);
        assertEquals(idCliente, clienteResponse.getIdCliente());
        assertEquals("Juan Perez", clienteResponse.getNombre());
        assertEquals("juan.perez@example.com", clienteResponse.getEmail());
        assertEquals(fechaAlta, clienteResponse.getFechaAlta());

        verify(clienteRepository, times(1)).findById(idCliente);
        verify(featureFlagService, times(1)).isMaskPiiActivo(idCliente.toString());
    }



}
