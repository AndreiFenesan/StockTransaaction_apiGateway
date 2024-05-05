package com.example.gateway.service;

import com.example.gateway.authorization.PermissionCheckRequestBody;
import com.example.gateway.authorization.PermissionResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final RestTemplate restTemplate;

    public Mono<PermissionResponseBody> checkPermission(String authToken, String role) {
        try {
            log.info("Checking permission, role: {}", role);
            var response = WebClient
                    .create()
                    .post()
                    .uri("http://localhost:8001/api/v1/authorization")
                    .body(BodyInserters.fromValue(new PermissionCheckRequestBody(role)))
                    .header(HttpHeaders.AUTHORIZATION, authToken)
                    .exchangeToMono(clientResponse ->
                            clientResponse.statusCode().is2xxSuccessful() ?
                                    clientResponse.bodyToMono(PermissionResponseBody.class)
                                    : Mono.just(new PermissionResponseBody(null)));


            log.info("Response: {}", response);
            return response;


        } catch (Exception e) {
            log.error("Error in receiving permission: {}", role, e);
        }
        return Mono.empty();
    }

    private MultiValueMap<String, String> buildHeaders(String authToken) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        return headers;
    }
}
