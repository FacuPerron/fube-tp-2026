package com.tiendaonline.clientes.service;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.UUID;
import org.springframework.stereotype.Service;

import com.tiendaonline.clientes.model.Cliente;
import com.tiendaonline.clientes.model.ClienteDTORequest;
import com.tiendaonline.clientes.model.ClienteDTOResponse;
import com.tiendaonline.clientes.repository.ClienteRepository;

@Service 
public class ClienteService {
    ClienteRepository clienteRepository; //invocamos al repositorio para registrar un cliente.
    

    //Me va a llegar un clienteRequestDTO y lo voy a transformar en un cliente (q es la entidad) para poder guardarlo en la base de datos.

    //Registrar un cliente.
    public ClienteDTOResponse registrarCliente(ClienteDTORequest clienteDTO){
        
        //creo el cliente, le seteo los valores que vienen del clienteDTORequest y la fecha de alta con la fecha actual.
        Cliente cliente = new Cliente();
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setFechaAlta(LocalDateTime.now());

        cliente = clienteRepository.save(cliente); //guardo el cliente en la base de datos y me devuelve el cliente con el ID generado.

        ClienteDTOResponse clienteResponse = new ClienteDTOResponse(); //creo la response para devolverla en la respuesta de la API.
        clienteResponse.setIdCliente(cliente.getIdCliente());
        clienteResponse.setNombre(cliente.getNombre());
        clienteResponse.setEmail(cliente.getEmail());
        clienteResponse.setFechaAlta(cliente.getFechaAlta());

        return clienteResponse;
    }

    //Obtener un cliente por su ID.
    public ClienteDTOResponse obtenerClientePorId(UUID idCliente) {

        //busco el cliente en la base de datos por su ID, si no lo encuentra se lanza una excepción.
        Cliente cliente = clienteRepository.findById(idCliente).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + idCliente));

        //creo la response para devolverla en la respuesta de la API.
        ClienteDTOResponse clienteResponse = new ClienteDTOResponse();
        clienteResponse.setIdCliente(cliente.getIdCliente());
        clienteResponse.setNombre(cliente.getNombre());
        clienteResponse.setEmail(cliente.getEmail());
        clienteResponse.setFechaAlta(cliente.getFechaAlta());

        return clienteResponse;
    }

    
}
