package com.tiendaonline.clientes.model;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ClienteDTOResponse {
    private UUID idCliente;
    private String nombre;
    private String email;
    private LocalDateTime fechaAlta;
}
