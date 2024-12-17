package com.example.reto.soporte.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.reto.soporte.model.dto.ClienteDTO;
import com.example.reto.soporte.service.SoporteClienteService;
import com.example.reto.soporte.util.ClienteNoEncontradoException;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/clientes")
@Slf4j
public class ClienteController {

	
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

	private final SoporteClienteService soporteClienteService;

    @Autowired
    public ClienteController(SoporteClienteService soporteClienteService) {
        this.soporteClienteService = soporteClienteService;
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo cliente")
    public Mono<ResponseEntity<ClienteDTO>> registrarCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        return soporteClienteService.registrarCliente(clienteDTO)
            .map(clienteRegistrado -> ResponseEntity.status(HttpStatus.CREATED).body(clienteRegistrado))
            .onErrorResume(ex -> handleError(ex)); // Cambia a una lambda explícita
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente por ID")
    public Mono<ResponseEntity<ClienteDTO>> actualizarCliente(
        @PathVariable Long id, 
        @Valid @RequestBody ClienteDTO clienteDTO
    ) {
        return soporteClienteService.actualizarCliente(id, clienteDTO)
            .map(ResponseEntity::ok)
            .onErrorResume(ex -> handleError(ex)); // Cambia a una lambda explícita
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente por ID")
    public Mono<ResponseEntity<Void>> eliminarCliente(@PathVariable Long id) {
        return soporteClienteService.eliminarCliente(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
            .onErrorResume(this::handleErrorDelete);
    }

    @GetMapping("/por-nombre")
    @Operation(summary = "Buscar clientes por nombre")
    public Flux<ClienteDTO> buscarPorNombre(@RequestParam String nombre) {
        return soporteClienteService.buscarPorNombre(nombre);
    }

    @GetMapping("/por-email")
    @Operation(summary = "Buscar clientes por email")
    public Flux<ClienteDTO> buscarPorEmail(@RequestParam String email) {
        return soporteClienteService.buscarPorEmail(email);
    }

    // Método de manejo de errores genérico
    private Mono<ResponseEntity<ClienteDTO>> handleError(Throwable ex) {
        log.error("Error en la operación de cliente", ex);
        
        if (ex instanceof ClienteNoEncontradoException) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    
    private Mono<ResponseEntity<Void>> handleErrorDelete(Throwable ex) {
        log.error("Error al eliminar cliente", ex);
        
        if (ex instanceof ClienteNoEncontradoException) {
            return Mono.just(ResponseEntity.notFound().<Void>build());
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
    }
    
}
