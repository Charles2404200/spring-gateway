package com.company.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    public AuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || authHeader.isEmpty()) {
                return chain.filter(exchange);
            }
            if (authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}

