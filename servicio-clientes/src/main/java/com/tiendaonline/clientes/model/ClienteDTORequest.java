package com.tiendaonline.clientes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ClienteDTORequest {
    String nombre;
    String email;
}
