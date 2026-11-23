package com.tiendaonline.inventario.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tiendaonline.inventario.model.enume.EstadoReserva;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID idReserva;

    @Column(name = "producto_id", nullable = false)
    private UUID idProducto;

    @Column(name = "pedido_id", nullable = false)
    private UUID idPedido;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
