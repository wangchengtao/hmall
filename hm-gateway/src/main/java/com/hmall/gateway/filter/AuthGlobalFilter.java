package com.hmall.gateway.filter;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@EnableConfigurationProperties(AuthProperties.class)
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取 Request
        ServerHttpRequest request = exchange.getRequest();

        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("authorization");


        Long userId;

        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String userInfo = userId.toString();

        return chain.filter(
                exchange.mutate()
                        .request(b -> b.header("user-info", userInfo))
                        .build()
        );

    }

    private boolean isExclude(String string) {
        return authProperties.getExcludePaths().stream().anyMatch(path -> antPathMatcher.match(path, string));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
