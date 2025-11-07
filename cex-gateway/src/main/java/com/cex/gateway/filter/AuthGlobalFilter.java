package com.cex.gateway.filter;

import com.cex.common.core.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import com.cex.gateway.config.GatewayAuthProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * 网关全局鉴权过滤器
 * 
 * 功能：
 * 1. 白名单检查
 * 2. JWT Token验证
 * 3. redis黑名单检查
 * 4. 用户信息传递到下游服务
 * 
 * @author cex
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private GatewayAuthProperties authProperties;

    // 极简兜底白名单（确保健康检查可用、防自锁）
    private static final List<String> FALLBACK_WHITELIST = Arrays.asList(
        "/actuator/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 检查白名单（配置优先，其次兜底）
        if (isWhiteList(path)) {
            log.debug("白名单路径，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 提取Token
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("请求缺少Token: {}", path);
            return unauthorized(exchange, "未授权，请先登录");
        }

        // 3. 验证Token
        return validateToken(token)
            .flatMap(claims -> {
                // 4. 检查黑名单
                return checkBlacklist(token)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("Token在黑名单中: userId={}", claims.get("userId"));
                            return unauthorized(exchange, "Token已失效，请重新登录");
                        }

                        // 5. 添加用户信息到请求头
                        ServerHttpRequest modifiedRequest = addUserHeaders(request, claims);
                        ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();

                        log.debug("鉴权通过，放行请求: path={}, userId={}", path, claims.get("userId"));
                        return chain.filter(modifiedExchange);
                    });
            })
            .onErrorResume(e -> {
                log.error("Token验证失败: {}", e.getMessage());
                return unauthorized(exchange, "Token验证失败: " + e.getMessage());
            });
    }

    /**
     * 检查路径是否在白名单
     */
    private boolean isWhiteList(String path) {
        List<String> wl = authProperties != null ? authProperties.getWhiteList() : null;
        if (wl == null || wl.isEmpty()) {
            wl = FALLBACK_WHITELIST;
        }
        return wl.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求头提取Token
     * 格式：Authorization: Bearer <token>
     */
    private String getToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 验证Token有效性
     */
    private Mono<Claims> validateToken(String token) {
        try {
            if (!JwtUtils.validateToken(token)) {
                return Mono.error(new RuntimeException("Token已过期或无效"));
            }
            Claims claims = JwtUtils.getClaimsFromToken(token);
            return Mono.just(claims);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Token解析失败: " + e.getMessage()));
        }
    }

    /**
     * 检查Token是否在黑名单
     * 使用SHA-256哈希Token，避免Key过长
     */
    private Mono<Boolean> checkBlacklist(String token) {
        try {
            String tokenHash = sha256(token);
            String key = "jwt:blacklist:" + tokenHash;
            return redisTemplate.hasKey(key)
                .defaultIfEmpty(false);
        } catch (Exception e) {
            log.error("检查黑名单失败: {}", e.getMessage());
            // 检查失败时，为了安全起见，认为Token无效
            return Mono.just(true);
        }
    }

    /**
     * 添加用户信息到请求头
     * 下游服务可以从请求头获取用户信息
     */
    private ServerHttpRequest addUserHeaders(ServerHttpRequest request, Claims claims) {
        return request.mutate()
            .header("X-User-Id", String.valueOf(claims.get("userId")))
            .header("X-User-Name", String.valueOf(claims.get("username")))
            .header("X-User-Level", String.valueOf(claims.get("level")))
            .header("X-User-Verified", String.valueOf(claims.get("verified")))
            .build();
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String body = String.format(
            "{\"code\":401,\"message\":\"%s\",\"data\":null}",
            message
        );

        DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * SHA-256哈希
     * 用于生成Token的黑名单Key
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256计算失败", e);
        }
    }

    @Override
    public int getOrder() {
        // 优先级：-100（在路由之前执行）
        return -100;
    }
}

