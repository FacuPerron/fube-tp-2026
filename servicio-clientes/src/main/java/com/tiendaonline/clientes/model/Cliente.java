package com.tiendaonline.clientes.model;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.UUID; //Cualquier error, cambiar a java.util.UUID

import com.google.type.DateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //getters, setters, toString, equals, hashCode.
@AllArgsConstructor //constructor con todos los atributos
@NoArgsConstructor //constructor sin atributos.
@Entity //es unatabla de la bdd
@Table(name = "clientes")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column (name = "id")
    private UUID idCliente;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

}