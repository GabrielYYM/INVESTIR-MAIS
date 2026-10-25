package com.repositorio.investir_mais.infrastructure;

import java.io.IOException;
import java.util.List;

import com.repositorio.investir_mais.infrastructure.config.AuthenticationRateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@RequiredArgsConstructor
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {
    private static final DefaultRedisScript<Long> CONSUME = new DefaultRedisScript<>("""
        local count = tonumber(redis.call('GET', KEYS[1]) or '0')
        if count >= tonumber(ARGV[1]) then
            return math.max(1, redis.call('PTTL', KEYS[1]))
        end
        if count == 0 then
            redis.call('SET', KEYS[1], 1, 'PX', ARGV[2])
        else
            redis.call('INCR', KEYS[1])
        end
        return 0
        """, Long.class);
    private static final RequestMatcher AUTHENTICATION_REQUESTS = new OrRequestMatcher(
        pathPattern(HttpMethod.POST, "/auth/login"),
        pathPattern(HttpMethod.POST, "/auth/code"),
        pathPattern(HttpMethod.POST, "/auth/verify-2fa"),
        pathPattern(HttpMethod.POST, "/api/users/register"),
        pathPattern(HttpMethod.POST, "/api/users/forgot-password"),
        pathPattern(HttpMethod.POST, "/api/users/reset-password"),
        pathPattern(HttpMethod.POST, "/api/users/resend-verification"));
    private final StringRedisTemplate redis;
    private final AuthenticationRateLimitProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !AUTHENTICATION_REQUESTS.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String key = properties.namespace() + ":" + request.getRemoteAddr() + ":" + request.getRequestURI();
        Long retryAfter;
        try {
            retryAfter = redis.execute(CONSUME, List.of(key),
                    Integer.toString(properties.capacity()), Long.toString(properties.refillPeriod().toMillis()));
        } catch (DataAccessException exception) {
            log.error("Redis indisponível ao verificar o limite de autenticação", exception);
            retryAfter = null;
        }
        if (retryAfter == null) {
            response.setStatus(503);
            response.setHeader("Retry-After", "1");
            response.setContentType("application/problem+json;charset=UTF-8");
            response.getWriter().write("{\"title\":\"Autenticação temporariamente indisponível\",\"status\":503}");
            return;
        }
        if (retryAfter > 0) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString((retryAfter + 999) / 1000));
            response.setContentType("application/problem+json;charset=UTF-8");
            response.getWriter().write("{\"title\":\"Muitas tentativas\",\"status\":429}");
            return;
        }
        chain.doFilter(request, response);
    }
}
