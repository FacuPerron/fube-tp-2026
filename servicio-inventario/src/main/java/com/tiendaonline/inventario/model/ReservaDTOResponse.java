package com.tiendaonline.inventario.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tiendaonline.inventario.model.enume.EstadoReserva;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ReservaDTOResponse {
    private UUID idReserva;
    private UUID idProducto;
    private UUID idPedido;
    private Integer cantidad;
    private EstadoReserva Estado;
    private LocalDateTime fechaCreacion;
}
