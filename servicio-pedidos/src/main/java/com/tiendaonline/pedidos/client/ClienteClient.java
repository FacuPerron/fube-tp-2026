package com.tiendaonline.pedidos.client;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component 
public class ClienteClient {
    //Cliente Http. Abre la conexión con servicio-clientes.
    private final RestClient restClient;

    //Inicializamos la conexión con servicio-clientes.
    //Configuto el RestClient con la dirección a la que vamos a apuntar.
    public ClienteClient(@Value("${servicios.clientes.url}") String baseUrl){
        this.restClient = RestClient.create(baseUrl);
    }

    //Uso el restClient para mandar la petición Http hacia servicio-clientes
    //Cuando vuelve la respuesta, onStatus revisa el código Http, si devuelve un 404, se corta el flujo lanzando una excepcion.
    public void verificarClienteExiste(UUID idCliente){
        restClient.get().uri("/clientes/{id}",idCliente).retrieve().onStatus(status -> status.value() == 404, (req,res) -> {
            throw new NoSuchElementException("Cliente no encontrado con ID: " + idCliente);
        }).toBodilessEntity();
    }


}
