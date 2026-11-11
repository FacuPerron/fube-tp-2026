package com.tiendaonline.clientes.repository;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tiendaonline.clientes.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    
}
