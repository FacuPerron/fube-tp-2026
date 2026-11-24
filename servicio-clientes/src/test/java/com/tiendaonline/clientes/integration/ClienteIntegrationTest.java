package com.tiendaonline.clientes.integration;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tiendaonline.clientes.client.PedidoClient.PedidoResponse;
import com.tiendaonline.clientes.model.Cliente;
import com.tiendaonline.clientes.repository.ClienteRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClienteIntegrationTest {

    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteRepository clienteRepository;

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.pedidos.url", () -> "http://localhost:" + mockServer.getPort());
    }

    @AfterAll
    static void detenerMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @BeforeEach
    @AfterEach
    void limpiar() {
        if (mockServer != null) {
            mockServer.reset();
        }
        clienteRepository.deleteAll();
    }

    //Implementamos el test de integración.
    //Caso de éxito.
    @Test
    void obtenerPedidosDeCliente_CuandoExisteYPedidosRespondeOk_DeberiaRetornarListaDePedidos() {
        //Guardamos un cliente real en clientes_db
        Cliente cliente = new Cliente();
        cliente.setNombre("Facundo Perron");
        cliente.setEmail("facundo.test@gmail.com");
        cliente.setFechaAlta(LocalDateTime.now());
        cliente = clienteRepository.save(cliente);
        UUID idCliente = cliente.getIdCliente(); // Si en tu entidad el getter es getIdCliente(), cambialo aquí

        //Simulamos la respuesta HTTP de servicio-pedidos en MockServer
        UUID idPedidoSimulado = UUID.randomUUID();
        String jsonPedidosSimulados = """
                [
                  {
                    "idPedido": "%s",
                    "idCliente": "%s",
                    "estado": "CREADO",
                    "montoTotal": 85000.00,
                    "fechaCreacion": "2026-11-24T12:00:00",
                    "fechaActualizacion": "2026-11-24T12:00:00",
                    "items": []
                  }
                ]
                """.formatted(idPedidoSimulado, idCliente);

        mockServer.when(request().withMethod("GET").withPath("/pedidos").withQueryStringParameter("idCliente", idCliente.toString())).respond(
                    response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody(jsonPedidosSimulados));

        //Ejecutamos la petición real contra nuestro ClienteController
        ResponseEntity<List<PedidoResponse>> respuesta = restTemplate.exchange(
                "/clientes/" + idCliente + "/pedidos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PedidoResponse>>() {}
        );

        //Verificamos el código HTTP y los datos recibidos desde MockServer
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        assertEquals(idPedidoSimulado, respuesta.getBody().get(0).idPedido());
        assertEquals("CREADO", respuesta.getBody().get(0).estado());
        assertEquals(new BigDecimal("85000.00"), respuesta.getBody().get(0).montoTotal());
    }

    //En el caso en que no exista el cliente...
    @Test
    void obtenerPedidosDeCliente_CuandoClienteNoExiste_DeberiaRetornar404() {
        UUID idInexistente = UUID.randomUUID();

        ResponseEntity<String> respuesta = restTemplate.getForEntity(
                "/clientes/" + idInexistente + "/pedidos",
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }



}
