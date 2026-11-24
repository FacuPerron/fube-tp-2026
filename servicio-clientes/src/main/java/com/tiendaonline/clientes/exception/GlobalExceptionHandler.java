package com.tiendaonline.clientes.exception;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {


    //Manejo de errores para cuando se busca una entidad y no se encuentra.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDTOResponse> manejarElementoNoEncontrado(NoSuchElementException ex) {
        ErrorDTOResponse error = new ErrorDTOResponse(HttpStatus.NOT_FOUND.value(),ex.getMessage(),LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    //Manejo de errores para IllegalArgumentException.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTOResponse> manejarArgumentoInvalido(IllegalArgumentException ex) {
        ErrorDTOResponse error = new ErrorDTOResponse(HttpStatus.BAD_REQUEST.value(),ex.getMessage(),LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    //Manejo de errores para las Requests.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTOResponse> manejarValidacionesDTO(MethodArgumentNotValidException ex) {
        //Obtenemos los detalles del error, por ejemplo los @NotBlank o @Email.
        String detalles = ex.getBindingResult().getFieldErrors().stream().map(err -> err.getField() + ": " + err.getDefaultMessage()).collect(Collectors.joining(", "));

        ErrorDTOResponse error = new ErrorDTOResponse(HttpStatus.BAD_REQUEST.value(),detalles,LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    //Manejo de errores de cualquier otro tipo.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTOResponse> manejarErrorGeneral(Exception ex) {
        ErrorDTOResponse error = new ErrorDTOResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Ocurrió un error interno en el servidor: " + ex.getMessage(),LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }



}
