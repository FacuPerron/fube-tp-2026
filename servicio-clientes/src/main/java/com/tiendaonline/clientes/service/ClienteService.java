package com.tiendaonline.clientes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.tiendaonline.clientes.client.PedidoClient;
import com.tiendaonline.clientes.client.PedidoClient.PedidoResponse;
import com.tiendaonline.clientes.model.Cliente;
import com.tiendaonline.clientes.model.ClienteDTORequest;
import com.tiendaonline.clientes.model.ClienteDTOResponse;
import com.tiendaonline.clientes.repository.ClienteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ClienteService {

    private final ClienteRepository clienteRepository; //invocamos al repositorio para registrar un cliente.
    private final PedidoClient pedidoClient;

    //Me va a llegar un clienteRequestDTO y lo voy a transformar en un cliente (q es la entidad) para poder guardarlo en la base de datos.
    @Transactional
    public ClienteDTOResponse registrarCliente(ClienteDTORequest clienteDTO){
        //Creo un cliente a partir del clienteDTORequest que me llega, para guardarlo en la base de datos.
        

        //Aunque ya lo valide en el Controller, vuelvo a validar que el nombre y el email no sean nulos o vacíos, si lo son lanzo una excepción.
        if(clienteDTO.getNombre() == null || clienteDTO.getNombre().trim().isEmpty() || clienteDTO.getEmail() == null || clienteDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre y el email son obligatorios");
        }

        //Antes de crear y guardar, verifico si el email ya está registrado en la base de datos, si es así lanzo la excepción.
        if(clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        //Creamos el cliente, le seteamos los valores que vienen del clienteDTORequest y la fecha de alta con la fecha actual.
        Cliente cliente = new Cliente();
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setFechaAlta(LocalDateTime.now());

        //Guardamos el cliente en la base de datos y nos devuelve el cliente con el ID generado.
        cliente = clienteRepository.save(cliente); 

        //Creamos la response para devolverla en la respuesta de la API.
        ClienteDTOResponse clienteResponse = new ClienteDTOResponse(); 
        clienteResponse.setIdCliente(cliente.getIdCliente());
        clienteResponse.setNombre(cliente.getNombre());
        clienteResponse.setEmail(cliente.getEmail());
        clienteResponse.setFechaAlta(cliente.getFechaAlta());

        return clienteResponse;
    }

    public ClienteDTOResponse obtenerClientePorId(UUID idCliente) {
        //Busco el cliente en la base de datos por su ID, si no lo encuentra se lanza una excepción.
        Cliente cliente = clienteRepository.findById(idCliente).orElseThrow(() -> new NoSuchElementException("Cliente no encontrado con ID: " + idCliente));

        //Creo la response para devolverla en la respuesta de la API.
        ClienteDTOResponse clienteResponse = new ClienteDTOResponse();
        clienteResponse.setIdCliente(cliente.getIdCliente());
        clienteResponse.setNombre(cliente.getNombre());
        clienteResponse.setEmail(cliente.getEmail());
        clienteResponse.setFechaAlta(cliente.getFechaAlta());

        return clienteResponse;
    }

    // Retornar los pedidos de un cliente determinado, que devuelve el listado de pedidos de un cliente consultando de forma síncrona a servicio-pedidos.
    public List<PedidoResponse> obtenerPedidosPorCliente(UUID idCliente) {
        
        if(!clienteRepository.existsById(idCliente)){
            throw new NoSuchElementException("Cliente no encontrado con ID: " + idCliente);
        }
        
        return pedidoClient.obtenerPedidosPorCliente(idCliente);
    }
    
}
