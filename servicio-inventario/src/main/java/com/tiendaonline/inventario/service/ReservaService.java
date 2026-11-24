package com.tiendaonline.inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.inventario.model.Producto;
import com.tiendaonline.inventario.model.Reserva;
import com.tiendaonline.inventario.model.ReservaDTORequest;
import com.tiendaonline.inventario.model.ReservaDTOResponse;
import com.tiendaonline.inventario.model.enume.EstadoReserva;
import com.tiendaonline.inventario.repository.ReservaRepository;
import com.tiendaonline.inventario.repository.ProductoRepository;

public class ReservaService {

    ReservaRepository reservaRepository;
    ProductoRepository productoRepository;

    @Transactional 
    //Metodo para reservar unidades de stock para un pedido.
    public ReservaDTOResponse reservarStock(ReservaDTORequest request) {
        
        Producto producto = productoRepository.findById(request.getIdProducto()).orElseThrow(() ->
                         new NoSuchElementException("Producto no encontrado con ID: " + request.getIdProducto()));
        
        Integer cantidadDisponible = producto.getCantidadDisponible();
        
        if(cantidadDisponible < request.getCantidad()) {
            throw new IllegalArgumentException("No hay stock suficiente para reservar");
        }

        Reserva reserva = new Reserva();
        reserva.setIdProducto(request.getIdProducto());
        reserva.setIdPedido(request.getIdPedido());
        reserva.setCantidad(request.getCantidad());
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());

        reservaRepository.save(reserva);

        //Actualizamos la cantidad disponible del producto.
        producto.setCantidadDisponible(cantidadDisponible - request.getCantidad());
        productoRepository.save(producto);
        
        return new ReservaDTOResponse(reserva.getIdReserva(),reserva.getIdProducto(),
                                        reserva.getIdPedido(),reserva.getCantidad(), 
                                        reserva.getEstado(),reserva.getFechaCreacion());
    }

    //Metodo para liberar una reserva existente.
    public Boolean liberarReserva(UUID idReserva){

        Reserva reserva = reservaRepository.findById(idReserva).orElseThrow(() -> new NoSuchElementException("Reserva no encontrada con ID de pedido"));

        if(reserva.getEstado() == EstadoReserva.LIBERADA){
            //Tiro excepción
            throw new IllegalArgumentException("Esta reserva ya está liberada.");
        }

        reserva.setEstado(EstadoReserva.LIBERADA);
        Producto producto = productoRepository.findById(reserva.getIdProducto()).orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID de producto: " + reserva.getIdProducto()));

        producto.setCantidadDisponible(reserva.getCantidad() + producto.getCantidadDisponible());

        productoRepository.save(producto);
        reservaRepository.save(reserva);

        return true;
    }

    public List<ReservaDTOResponse> obtenerReservasActivasPorPedido(UUID idPedido) {

        List<Reserva> reservas = reservaRepository.findByIdPedidoAndEstado(idPedido, EstadoReserva.ACTIVA);

        return reservas.stream().map(reserva -> new ReservaDTOResponse(reserva.getIdReserva(),reserva.getIdProducto(),reserva.getIdPedido(),
                                    reserva.getCantidad(),reserva.getEstado(),reserva.getFechaCreacion())).toList();
}
}
