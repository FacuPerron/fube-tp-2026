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

import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository; //invocamos al repositorio para registrar un cliente.
    private final PedidoClient pedidoClient;
    private final FeatureFlagService featureFlagService;

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
        log.info("Cliente registrado {}", kv("clienteId", cliente.getIdCliente()));

        //Creamos la response para devolverla en la respuesta de la API.
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
        
        log.info("Consultando pedidos del cliente {}", kv("clienteId", idCliente));
        return pedidoClient.obtenerPedidosPorCliente(idCliente);
    }
    

    public ClienteDTOResponse obtenerClientePorId(UUID idCliente) {
        //Busco el cliente en la base de datos por su ID, si no lo encuentra se lanza una excepción.
        Cliente cliente = clienteRepository.findById(idCliente).orElseThrow(() -> {
            log.warn("Cliente no encontrado {}", kv("clienteId", idCliente));
            return new NoSuchElementException("Cliente no encontrado con ID: " + idCliente);
        });

        String nombre = cliente.getNombre();
        String email = cliente.getEmail();

        //Si la flag esta activa, procedo a enmascarar el nombre y el email.
        if(featureFlagService.isMaskPiiActivo(idCliente.toString())){
            nombre = this.enmascararNombre(nombre);
            email = this.enmascararEmail(email);
        }


        //Creo la response para devolverla en la respuesta de la API.
        ClienteDTOResponse clienteResponse = new ClienteDTOResponse();
        clienteResponse.setIdCliente(cliente.getIdCliente());
        clienteResponse.setNombre(nombre);
        clienteResponse.setEmail(email);
        clienteResponse.setFechaAlta(cliente.getFechaAlta());

        return clienteResponse;
    }

    //Enmascaramos el nombre dejando solo la primera letra de cada palabra.
    private String enmascararNombre(String nombre) {
        
        if (nombre == null || nombre.isBlank()) {
            return "";
        }

        String[] palabras = nombre.trim().split(" ");
        String nombreEnmascarado = "";

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                nombreEnmascarado += palabra.charAt(0) + "*** ";
            }
        }
        //Le sacamos el último espacio que sobra al final.
        return nombreEnmascarado.trim();
        
    }


    //Enmascaramos el email dejando solo la extensión final.
    private String enmascararEmail(String email) {
        int posPunto = email.lastIndexOf(".");
        if (posPunto == -1) {
            return "****@****";
        }
        //Devuelvo el ****@**** concatenado con lo que hay a la derecha del . (.com)
        return "****@****" + email.substring(posPunto);
    }
}
