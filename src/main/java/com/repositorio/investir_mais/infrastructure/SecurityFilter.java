package com.repositorio.investir_mais.infrastructure;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties.RateLimit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;
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
    private static final List<LimitedRoute> AUTHENTICATION_REQUESTS = List.of(
        new LimitedRoute("/auth/login"),
        new LimitedRoute("/auth/code"),
        new LimitedRoute("/auth/verify-2fa"),
        new LimitedRoute("/api/users/register"),
        new LimitedRoute("/api/users/verify-email"),
        new LimitedRoute("/api/users/forgot-password"),
        new LimitedRoute("/api/users/reset-password"),
        new LimitedRoute("/api/users/resend-verification"));
    private final StringRedisTemplate redis;
    private final RateLimit properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var route = AUTHENTICATION_REQUESTS.stream().filter(candidate -> candidate.matcher().matches(request)).findFirst();
        if (route.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        String path = route.get().path();
        String key = properties.namespace() + ":" + request.getRemoteAddr() + ":" + path;
        Long retryAfter;
        try {
            retryAfter = consume(key);
            if (Long.valueOf(0).equals(retryAfter) && "/auth/login".equals(path)) {
                String email = request.getParameter("email");
                if (email != null && !email.isBlank() && email.length() <= 320) {
                    String account = DigestUtils.sha256Hex(email.strip().toLowerCase(Locale.ROOT));
                    retryAfter = consume(properties.namespace() + ":account:" + account + ":" + path);
                }
            }
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

    private Long consume(String key) {
        return redis.execute(CONSUME, List.of(key), Integer.toString(properties.capacity()),
                Long.toString(properties.refillPeriod().toMillis()));
    }

    private record LimitedRoute(String path, RequestMatcher matcher) {
        LimitedRoute(String path) {
            this(path, pathPattern(HttpMethod.POST, path));
        }
    }
}
