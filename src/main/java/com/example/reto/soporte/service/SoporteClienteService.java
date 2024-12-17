package com.example.reto.soporte.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.reto.soporte.client.GoRestClient;
import com.example.reto.soporte.model.Cliente;
import com.example.reto.soporte.model.dto.ClienteDTO;
import com.example.reto.soporte.repository.ClienteRepository;
import com.example.reto.soporte.util.ClienteNoEncontradoException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class SoporteClienteService {
	
    private static final Logger log = LoggerFactory.getLogger(SoporteClienteService.class);
	
	private final ClienteRepository clienteRepository;
    private final GoRestClient goRestClient;
    private final ModelMapper modelMapper;

    @Autowired
    public SoporteClienteService(
        ClienteRepository clienteRepository,
        GoRestClient goRestClient,
        ModelMapper modelMapper
    ) {
        this.clienteRepository = clienteRepository;
        this.goRestClient = goRestClient;
        this.modelMapper = modelMapper;
    }

    public Mono<ClienteDTO> registrarCliente(ClienteDTO clienteDTO) {
        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class);
        
        return goRestClient.buscarUsuario(cliente.getName(), cliente.getEmail())
            .flatMap(usuarioGoRest -> {
                cliente.setStatus("exists");
                return clienteRepository.save(cliente)
                    .map(clienteGuardado -> modelMapper.map(clienteGuardado, ClienteDTO.class));
            })
            .switchIfEmpty(Mono.defer(() -> {
                cliente.setStatus("active");
                return clienteRepository.save(cliente)
                    .map(clienteGuardado -> modelMapper.map(clienteGuardado, ClienteDTO.class));
            }))
            .doOnError(ex -> log.error("Error al registrar cliente", ex));
    }

    public Mono<ClienteDTO> actualizarCliente(Long id, ClienteDTO clienteDTO) {
        return clienteRepository.findById(id)
            .flatMap(clienteExistente -> {
                clienteExistente.setName(clienteDTO.getName());
                clienteExistente.setEmail(clienteDTO.getEmail());
                clienteExistente.preUpdate();
                return clienteRepository.save(clienteExistente);
            })
            .map(clienteActualizado -> modelMapper.map(clienteActualizado, ClienteDTO.class))
            .switchIfEmpty(Mono.error(new ClienteNoEncontradoException("Cliente no encontrado")));
    }
    
    public Mono<Void> eliminarCliente(Long id) {
        return clienteRepository.findById(id)
            .switchIfEmpty(Mono.error(new ClienteNoEncontradoException("Cliente no encontrado con ID: " + id)))
            .flatMap(cliente -> clienteRepository.delete(cliente));
    }

    public Flux<ClienteDTO> buscarPorNombre(String nombre) {
        return clienteRepository.findByNameContaining(nombre)
            .map(cliente -> modelMapper.map(cliente, ClienteDTO.class));
    }

    public Flux<ClienteDTO> buscarPorEmail(String email) {
        return clienteRepository.findByEmailContaining(email)
            .map(cliente -> modelMapper.map(cliente, ClienteDTO.class));
    }

}
