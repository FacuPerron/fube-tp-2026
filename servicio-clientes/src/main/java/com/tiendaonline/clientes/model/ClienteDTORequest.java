package com.tiendaonline.clientes.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ClienteDTORequest {

    @NotBlank(message = "El nombre es obligatorio.")
    String nombre;
    
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    String email;
}
