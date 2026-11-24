package com.tiendaonline.pedidos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.pedidos.model.PedidoDTORequest;
import com.tiendaonline.pedidos.model.PedidoDTOResponse;
import com.tiendaonline.pedidos.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PedidoController {
    
    private final PedidoService pedidoService;

    @PostMapping("/pedidos")
    public ResponseEntity<PedidoDTOResponse> crearPedido(@Valid @RequestBody PedidoDTORequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<PedidoDTOResponse> consultarPedido(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK

        ).body(pedidoService.obtenerPedidoPorId(id));
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoDTOResponse>> consultarPedidoCliente(@RequestParam UUID idCliente){
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.listarPedidosPorCliente(idCliente));
    }

    @PostMapping("/pedidos/{id}/cancelacion")
    public ResponseEntity<Void> cancelarPedido(@PathVariable UUID id){
        pedidoService.cancelarPedido(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
