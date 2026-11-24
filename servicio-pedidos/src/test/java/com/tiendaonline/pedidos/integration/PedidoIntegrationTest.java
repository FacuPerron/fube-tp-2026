package com.tiendaonline.pedidos.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.ItemPedidoDTORequest;
import com.tiendaonline.pedidos.model.Pedido;
import com.tiendaonline.pedidos.model.PedidoDTORequest;
import com.tiendaonline.pedidos.model.PedidoDTOResponse;
import com.tiendaonline.pedidos.repository.ItemPedidoRepository;
import com.tiendaonline.pedidos.repository.PedidoRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PedidoIntegrationTest {

    //Inicializamos MockServer en un puerto aleatorio antes de que levante el contexto de Spring
    private static final ClientAndServer mockServer = ClientAndServer.startClientAndServer();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    //Configuramos las URLs para que ambos clientes apunten al puerto simulado de MockServer.
    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("servicios.clientes.url", () -> "http://localhost:" + mockServer.getPort());
        registry.add("servicios.inventario.url", () -> "http://localhost:" + mockServer.getPort());
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
        //Borramos primero items_pedido y luego pedidos para respetar la clave foránea.
        itemPedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
    }

    @Test
    void crearPedido_CuandoClienteExisteYHayStock_DeberiaCrearPedidoYRetornar201() {
        //Datos simulados
        UUID idCliente = UUID.randomUUID();
        UUID idProducto = UUID.randomUUID();
        UUID idReservaSimulada = UUID.randomUUID();

        //Simulamos que servicio-clientes responde 200 OK (el cliente existe).
        mockServer.when(
                request().withMethod("GET").withPath("/clientes/" + idCliente)
        ).respond(
                response().withStatusCode(200)
                          .withContentType(MediaType.APPLICATION_JSON)
                          .withBody("""
                                  {
                                    "idCliente": "%s",
                                    "nombre": "Facundo Perron",
                                    "email": "facu@example.com"
                                  }
                                  """.formatted(idCliente))
        );

        //Simulamos que servicio-inventario reserva el stock y responde 201 Created.
        mockServer.when(
                request().withMethod("POST").withPath("/reservas")
        ).respond(
                response().withStatusCode(201)
                          .withContentType(MediaType.APPLICATION_JSON)
                          .withBody("""
                                  {
                                    "idReserva": "%s"
                                  }
                                  """.formatted(idReservaSimulada))
        );

        //Preparamos el request body para el pedido.
        ItemPedidoDTORequest itemRequest = new ItemPedidoDTORequest(idProducto, 2, new BigDecimal("150.00"));
        PedidoDTORequest request = new PedidoDTORequest(idCliente, List.of(itemRequest));

        //Ejecutamos la petición contra PedidoController.
        ResponseEntity<PedidoDTOResponse> respuesta = restTemplate.postForEntity(
                "/pedidos",
                request,
                PedidoDTOResponse.class
        );

        //Verificamos la respuesta HTTP.
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(idCliente, respuesta.getBody().getIdCliente());
        assertEquals(EstadoPedido.CREADO, respuesta.getBody().getEstado());
        assertEquals(new BigDecimal("300.00"), respuesta.getBody().getMontoTotal());
        assertEquals(1, respuesta.getBody().getItems().size());

        //Verificamos que se haya guardado efectivamente en la base de datos real pedidos_db.
        UUID idPedidoCreado = respuesta.getBody().getIdPedido();
        assertTrue(pedidoRepository.existsById(idPedidoCreado));
        assertEquals(1, itemPedidoRepository.findByIdPedido(idPedidoCreado).size());
    }

    @Test
    void crearPedido_CuandoClienteNoExiste_DeberiaRetornar404YNoGuardarPedido() {
        UUID idClienteInexistente = UUID.randomUUID();
        UUID idProducto = UUID.randomUUID();

        //Simulamos que servicio-clientes responde 404 Not Found.
        mockServer.when(
                request().withMethod("GET").withPath("/clientes/" + idClienteInexistente)
        ).respond(
                response().withStatusCode(404)
        );

        ItemPedidoDTORequest itemRequest = new ItemPedidoDTORequest(idProducto, 1, new BigDecimal("100.00"));
        PedidoDTORequest request = new PedidoDTORequest(idClienteInexistente, List.of(itemRequest));

        //Ejecutamos la petición contra PedidoController.
        ResponseEntity<String> respuesta = restTemplate.postForEntity(
                "/pedidos",
                request,
                String.class
        );

        //Verificamos que devuelva 404 Not Found y la base de datos quede vacía.
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    void crearPedido_CuandoNoHayStockSuficiente_DeberiaRetornar409YNoGuardarPedido() {
        UUID idCliente = UUID.randomUUID();
        UUID idProducto = UUID.randomUUID();

        //Simulamos que el cliente sí existe.
        mockServer.when(
                request().withMethod("GET").withPath("/clientes/" + idCliente)
        ).respond(
                response().withStatusCode(200)
        );

        //Simulamos que inventario rechaza por falta de stock (400 Bad Request o 409 Conflict).
        mockServer.when(
                request().withMethod("POST").withPath("/reservas")
        ).respond(
                response().withStatusCode(400)
                          .withContentType(MediaType.APPLICATION_JSON)
                          .withBody("""
                                  {
                                    "status": 400,
                                    "mensaje": "No hay stock suficiente para reservar"
                                  }
                                  """)
        );

        ItemPedidoDTORequest itemRequest = new ItemPedidoDTORequest(idProducto, 10, new BigDecimal("50.00"));
        PedidoDTORequest request = new PedidoDTORequest(idCliente, List.of(itemRequest));

        //Ejecutamos la petición contra PedidoController.
        ResponseEntity<String> respuesta = restTemplate.postForEntity(
                "/pedidos",
                request,
                String.class
        );

        //Verificamos que devuelva 409 Conflict y que la base de datos quede vacía.
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    void cancelarPedido_CuandoPedidoEstaEnEstadoCreado_DeberiaLiberarReservasYCambiarEstadoACancelado() {
        UUID idCliente = UUID.randomUUID();
        UUID idPedido = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();

        //Guardamos un pedido en estado CREADO directamente en pedidos_db.
        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setIdCliente(idCliente);
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setMontoTotal(new BigDecimal("200.00"));
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        //Simulamos que servicio-inventario devuelve la lista de reservas activas para ese pedido.
        mockServer.when(
                request().withMethod("GET")
                         .withPath("/reservas")
                         .withQueryStringParameter("idPedido", idPedido.toString())
        ).respond(
                response().withStatusCode(200)
                          .withContentType(MediaType.APPLICATION_JSON)
                          .withBody("""
                                  [
                                    {
                                      "idReserva": "%s"
                                    }
                                  ]
                                  """.formatted(idReserva))
        );

        //Simulamos que servicio-inventario responde 200 OK a la liberación.
        mockServer.when(
                request().withMethod("POST").withPath("/reservas/" + idReserva + "/liberacion")
        ).respond(
                response().withStatusCode(200)
        );

        //Ejecutamos la cancelación del pedido
        ResponseEntity<Void> respuesta = restTemplate.postForEntity(
                "/pedidos/" + idPedido + "/cancelacion",
                null,
                Void.class
        );

        //Verificamos respuesta 200 OK y que el estado en la base de datos haya pasado a CANCELADO.
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());

        Pedido pedidoActualizado = pedidoRepository.findById(idPedido).orElseThrow();
        assertEquals(EstadoPedido.CANCELADO, pedidoActualizado.getEstado());
    }
}
