package com.tiendaonline.clientes.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.clientes.client.PedidoClient.PedidoResponse;
import com.tiendaonline.clientes.model.ClienteDTORequest;
import com.tiendaonline.clientes.model.ClienteDTOResponse;
import com.tiendaonline.clientes.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping("/clientes")
    public ResponseEntity<ClienteDTOResponse> registrarCliente(@Valid @RequestBody ClienteDTORequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrarCliente(request));
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTOResponse> buscarClientePorId(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(clienteService.obtenerClientePorId(id));
    }

    @GetMapping("/clientes/{id}/pedidos")
    public ResponseEntity<List<PedidoResponse>> obtenerPedidosDeCliente(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(clienteService.obtenerPedidosPorCliente(id));
    }

}
