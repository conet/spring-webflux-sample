package com.sample.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/http2")
public class Http2TestController {
    private final WebClient webClient;

    private final Random RANDOM = new Random();

    public Http2TestController(WebClient.Builder webClientBuilder, ClientHttpConnector httpClientConnector) {
        this.webClient = webClientBuilder.clientConnector(httpClientConnector).baseUrl("https://httpbin.org").build();
    }

    @GetMapping
    public Mono<String> get() {
        return webClient
                .get()
                .uri("/delay/" + RANDOM.nextInt(10))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(ex -> {
                    log.warn("Call failed", ex);
                    return Mono.empty();
                });
    }
}
