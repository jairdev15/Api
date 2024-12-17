package com.example.reto.soporte.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.reto.soporte.model.Cliente;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ClienteRepository extends ReactiveCrudRepository<Cliente, Long>{
	
	Flux<Cliente> findByNameContaining(String nombre);
    Flux<Cliente> findByEmailContaining(String email);
    Mono<Cliente> findByName(String nombre);
    Mono<Cliente> findByEmail(String email);

}
