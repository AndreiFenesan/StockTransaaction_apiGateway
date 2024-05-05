package com.example.gateway.config;


import com.example.gateway.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class LocatorsConfig {

    private final AuthenticationService authenticationService;

    @Bean
    public RouteLocator authenticationServiceRouteLocator(RouteLocatorBuilder locatorBuilder) {
        return locatorBuilder.routes()
                .route(predicateSpec ->
                        predicateSpec
                                .path("/authentication-service/**")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec.stripPrefix(1))
                                .uri("lb://authentication-service"))
                .build();
    }

    @Bean
    public RouteLocator transactionServiceRouteLocator(RouteLocatorBuilder locatorBuilder) {
        return locatorBuilder.routes()
                .route(predicateSpec ->
                        predicateSpec
                                .path("/transaction-service/**")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec.stripPrefix(1)
                                        .filter(authenticateFilter("ROLE_BASIC_USER"))
                                )
                                .uri("lb://transaction-service"))
                .build();
    }

    @Bean
    public RouteLocator stockServiceRouteLocator(RouteLocatorBuilder locatorBuilder) {
        return locatorBuilder.routes()
                .route(predicateSpec ->
                        predicateSpec
                                .path("/stock-service/**")
                                .filters(gatewayFilterSpec -> gatewayFilterSpec.stripPrefix(1)
                                        .filter(authenticateFilter("ROLE_BASIC_USER"))
                                )
                                .uri("lb://stock-service"))
                .build();
    }

    public GatewayFilter authenticateFilter(String role) {
        return (exchange, chain) -> {
            var authToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authToken == null || authToken.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(403));
                return Mono.empty();
            }
            return authenticationService.checkPermission(authToken, role)
                    .flatMap(responseBody -> {
                        var userId = responseBody.getUserId();
                        if (responseBody.getUserId() != null) {
                            exchange.getRequest()
                                    .mutate()
                                    .headers(headers -> headers.add("x-user-id", userId));
                            return chain.filter(exchange);
                        }
                        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(403));
                        return Mono.empty();
                    });
        };
    }


}
