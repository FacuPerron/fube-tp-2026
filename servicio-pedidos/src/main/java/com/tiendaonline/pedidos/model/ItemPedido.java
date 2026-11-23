package com.tiendaonline.pedidos.model;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items_pedido")
public class ItemPedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID idItemPedido;

    @Column(name = "pedido_id", nullable = false)
    private UUID idPedido;

    @Column(name = "producto_id", nullable = false)
    private UUID idProducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private Double precioUnitario;
}
