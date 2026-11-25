package com.tiendaonline.clientes.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.clientes.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    boolean existsByEmail(String email);
    
}
