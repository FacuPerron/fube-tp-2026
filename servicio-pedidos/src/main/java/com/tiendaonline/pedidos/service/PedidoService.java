package com.tiendaonline.pedidos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tiendaonline.pedidos.client.ClienteClient;
import com.tiendaonline.pedidos.client.InventarioClient;
import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.ItemPedido;
import com.tiendaonline.pedidos.model.ItemPedidoDTORequest;
import com.tiendaonline.pedidos.model.ItemPedidoDTOResponse;
import com.tiendaonline.pedidos.model.Pedido;
import com.tiendaonline.pedidos.model.PedidoDTORequest;
import com.tiendaonline.pedidos.model.PedidoDTOResponse;
import com.tiendaonline.pedidos.repository.ItemPedidoRepository;
import com.tiendaonline.pedidos.repository.PedidoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteClient clienteClient;
    private final InventarioClient inventarioClient;

    @Transactional
    public PedidoDTOResponse crearPedido(PedidoDTORequest request){

        clienteClient.verificarClienteExiste(request.getIdCliente());


        UUID idPedido = UUID.randomUUID();

        List<UUID> reservasConfirmadas = new ArrayList<>();
        List<ItemPedido> itemsAGuardar = new ArrayList<>();
        BigDecimal montoTotal = BigDecimal.ZERO;

        try{
            for(ItemPedidoDTORequest itemReq: request.getItems()){
                UUID idReserva = inventarioClient.reservarStock(itemReq.getIdProducto(), idPedido, itemReq.getCantidad());
                reservasConfirmadas.add(idReserva);

                ItemPedido item = new ItemPedido();
                item.setIdPedido(idPedido);
                item.setIdProducto(itemReq.getIdProducto());
                item.setCantidad(itemReq.getCantidad());
                item.setPrecioUnitario(itemReq.getPrecioUnitario());

                itemsAGuardar.add(item);

                BigDecimal subtotal = itemReq.getPrecioUnitario().multiply(BigDecimal.valueOf(itemReq.getCantidad()));

                montoTotal = montoTotal.add(subtotal);
            }
        }catch(RuntimeException ex) {
            for(UUID idReservaPrevia : reservasConfirmadas){
                inventarioClient.liberarReserva(idReservaPrevia);
            }
            throw ex;
        }

        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setIdCliente(request.getIdCliente());
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setMontoTotal(montoTotal);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());

        pedido = pedidoRepository.save(pedido);
        itemsAGuardar = itemPedidoRepository.saveAll(itemsAGuardar);

        return this.mapearAResponse(pedido, itemsAGuardar);
        
    }

    @Transactional
    public PedidoDTOResponse cancelarPedido(UUID id){

        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + id));

        if(pedido.getEstado() != EstadoPedido.CREADO && pedido.getEstado() != EstadoPedido.CONFIRMADO){
            throw new IllegalStateException("Solo se pueden cancelar pedidos en estado CREADO o CONFIRMADO");
        }

        inventarioClient.liberarReservasPorPedido(id);

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(LocalDateTime.now());

        pedido = pedidoRepository.save(pedido);
        List<ItemPedido> items = itemPedidoRepository.findByIdPedido(id);

        return mapearAResponse(pedido, items);
    }

    public PedidoDTOResponse obtenerPedidoPorId(UUID id){
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + id));

        List<ItemPedido> items = itemPedidoRepository.findByIdPedido(id);
        return mapearAResponse(pedido, items);
    }

    public List<PedidoDTOResponse> listarPedidosPorCliente(UUID id) {

        List<Pedido> pedidos = pedidoRepository.findByIdCliente(id);
        return pedidos.stream().map(pedido -> mapearAResponse(pedido, itemPedidoRepository.findByIdPedido(pedido.getIdPedido()))).toList();

    }





    private PedidoDTOResponse mapearAResponse(Pedido pedido, List<ItemPedido> items) {
        List<ItemPedidoDTOResponse> itemsDTO = items.stream().map(item -> new ItemPedidoDTOResponse(item.getIdItemPedido(),
                                    item.getIdProducto(),item.getCantidad(),item.getPrecioUnitario())).toList();

        return new PedidoDTOResponse(pedido.getIdPedido(),pedido.getIdCliente(),pedido.getEstado(),pedido.getMontoTotal(),
                                    pedido.getFechaCreacion(),pedido.getFechaActualizacion(),itemsDTO);
    }

}
