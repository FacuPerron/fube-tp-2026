package com.tiendaonline.clientes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tiendaonline.clientes.model.Cliente;
import com.tiendaonline.clientes.model.ClienteDTORequest;
import com.tiendaonline.clientes.model.ClienteDTOResponse;
import com.tiendaonline.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

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

}
