package com.tiendaonline.inventario.controller;

import java.io.ObjectInputFilter.Status;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.inventario.model.ProductoDTOResponse;
import com.tiendaonline.inventario.model.ReservaDTORequest;
import com.tiendaonline.inventario.model.ReservaDTOResponse;
import com.tiendaonline.inventario.service.ProductoService;
import com.tiendaonline.inventario.service.ReservaService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@RestController 

public class ProductoController {

    ReservaService reservaService;
    ProductoService productoService;

    //Consultar disponibilidad de producto por su ID.
    @GetMapping("/productos/{id}/disponibilidad")
    public ResponseEntity<Integer> consultarStockDisponible(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.consultarStockDisponible(id));
    }

    //Consultar producto
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTOResponse> consultarProducto(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.consultarProducto(id));
    }

    //Reservar unidades de stock para un pedido.
    @PostMapping("/reservas")
    public ResponseEntity<ReservaDTOResponse> reservarStock(@Valid @RequestBody ReservaDTORequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.reservarStock(request));
    }

    //Liberar una reserva existente.
    @PostMapping("/reservas/{id}/liberacion")
    public ResponseEntity<Void> liberarReserva(@PathVariable UUID id) {
        reservaService.liberarReserva(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //Consultar reservas activas de un pedido.
    @GetMapping("/reservas")
    public ResponseEntity<List<ReservaDTOResponse>> obtenerReservasPorPedido(@RequestParam UUID idPedido) {
        return ResponseEntity.status(HttpStatus.OK).body(reservaService.obtenerReservasActivasPorPedido(idPedido));
    }
}
