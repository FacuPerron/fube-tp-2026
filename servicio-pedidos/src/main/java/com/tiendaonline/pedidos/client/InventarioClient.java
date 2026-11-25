package com.tiendaonline.pedidos.client;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component 
public class InventarioClient {
    private final RestClient restClient;

    public InventarioClient(RestClient.Builder restClientBuilder, @Value("${servicios.inventario.url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }
    
    public UUID reservarStock(UUID idProducto, UUID idPedido, Integer cantidad){
        ReservaRequest request = new ReservaRequest(idProducto,idPedido,cantidad);

        ReservaResponse response = restClient.post().uri("/reservas").body(request).
                        retrieve().onStatus(status -> status.value() == 404, (req,res) -> {
                        throw new NoSuchElementException("Producto no encontrado en inventario con ID: " + idProducto);
                        }).onStatus(status -> status.value() == 409 || status.value() == 400, (req, res) -> {
                        throw new IllegalStateException("No hay stock suficiente para el producto con ID: " + idProducto);
                        }).body(ReservaResponse.class);
       return response.idReserva(); 
        
    }

    public void liberarReserva(UUID reservaId) {
        restClient.post()
                        .uri("/reservas/{id}/liberacion", reservaId)
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new NoSuchElementException("Reserva no encontrada en inventario con ID: " + reservaId);
                        }).toBodilessEntity();
    }

    public void liberarReservasPorPedido(UUID idPedido) {
        ReservaResponse[] reservas = restClient.get()
                        .uri("/reservas?idPedido={idPedido}", idPedido)
                        .retrieve()
                        .body(ReservaResponse[].class);
        if (reservas != null) {
            for (ReservaResponse reserva : reservas) {
                this.liberarReserva(reserva.idReserva());
            }
        }
    }

    // Contratos internos para serializar/deserializar el JSON sin crear archivos DTO extra
    private record ReservaRequest(UUID idProducto, UUID idPedido, Integer cantidad) {}
    private record ReservaResponse(UUID idReserva) {}

}
