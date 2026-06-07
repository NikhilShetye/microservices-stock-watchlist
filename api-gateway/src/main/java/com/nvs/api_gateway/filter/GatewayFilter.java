package com.nvs.api_gateway.filter;

import com.nvs.api_gateway.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GatewayFilter implements GlobalFilter, Ordered {
        private final JwtUtil jwtUtil;

        public GatewayFilter(JwtUtil jwtUtil) {
                this.jwtUtil = jwtUtil;
        }

        private static final Logger log = LoggerFactory.getLogger(GatewayFilter.class);

        @Override
        public Mono<Void> filter(ServerWebExchange exchange,
                        GatewayFilterChain chain) {

                ServerHttpRequest request = exchange.getRequest();

                // 1. Skip auth APIs
                String path = request.getURI().getPath();

                if (path.contains("/auth/login") ||
                                path.contains("/auth/register")) {

                        return chain.filter(exchange);
                }

                // 2. Read Authorization header
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                // 3. Validate Authorization header
                if (authHeader == null ||
                                !authHeader.startsWith("Bearer ")) {

                        exchange.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse().setComplete();
                }

                // 4. Extract JWT token
                String token = authHeader.substring(7);

                // 5. Validate token
                if (!jwtUtil.validateToken(token)) {

                        log.warn("Unauthorized request to path: {}", path);

                        exchange.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse().setComplete();
                }

                // 6. Extract user info
                Long userId = jwtUtil.extractUserId(token);

                String role = jwtUtil.extractRole(token);

                // 7. Add headers
                ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", String.valueOf(userId))
                                .header("X-Role", role)
                                .build();

                log.info("JWT validated successfully");

                // 8. Forward request
                return chain.filter(
                                exchange.mutate()
                                                .request(modifiedRequest)
                                                .build());
        }

        @Override
        public int getOrder() {
                return -1;
        }
}