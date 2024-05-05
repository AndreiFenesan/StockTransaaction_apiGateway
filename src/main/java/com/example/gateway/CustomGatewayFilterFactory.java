package com.example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;

public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // Access the ServerWebExchange to retrieve the request headers
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            // Use the authorizationHeader as needed
            System.out.println("Authorization Header: " + authorizationHeader);

            // Continue the filter chain
            return chain.filter(exchange);
        };
    }
}
