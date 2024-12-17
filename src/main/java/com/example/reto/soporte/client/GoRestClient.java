package com.example.reto.soporte.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.reto.soporte.model.UsuarioGoRest;

import reactor.core.publisher.Mono;

@Component
public class GoRestClient {
	
	private final WebClient webClient;

    public GoRestClient(WebClient.Builder webClientBuilder, 
                        @Value("${gorest.api.url}") String gorestUrl) {
        this.webClient = webClientBuilder.baseUrl(gorestUrl).build();
    }

    public Mono<UsuarioGoRest> buscarUsuario(String nombre, String email) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("name", nombre)
                .queryParam("email", email)
                .build())
            .retrieve()
            .bodyToFlux(UsuarioGoRest.class)
            .next()
            .onErrorResume(ex -> Mono.empty());
    }

}
