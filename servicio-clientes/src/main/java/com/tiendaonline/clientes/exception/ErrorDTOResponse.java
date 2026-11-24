package com.tiendaonline.clientes.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor 
public class ErrorDTOResponse {
    private Integer status;
    private String mensaje;
    private LocalDateTime timeStamp;
}
