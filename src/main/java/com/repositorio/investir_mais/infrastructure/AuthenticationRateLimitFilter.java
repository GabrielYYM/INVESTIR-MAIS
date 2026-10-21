package com.repositorio.investir_mais.infrastructure;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@RequiredArgsConstructor
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {
    private static final RequestMatcher AUTHENTICATION_REQUESTS = new OrRequestMatcher(
        pathPattern(HttpMethod.POST, "/auth/login"),
        pathPattern(HttpMethod.POST, "/auth/code"),
        pathPattern(HttpMethod.POST, "/auth/verify-2fa"),
        pathPattern(HttpMethod.POST, "/api/users/register"),
        pathPattern(HttpMethod.POST, "/api/users/forgot-password"),
        pathPattern(HttpMethod.POST, "/api/users/reset-password"),
        pathPattern(HttpMethod.POST, "/api/users/resend-verification"));
    private final int capacity;
    private final AuthenticationRateLimitStore rateLimitStore;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !AUTHENTICATION_REQUESTS.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        if (!rateLimitStore.tryConsume(key, capacity)) {
            response.setStatus(429);
            response.setContentType("application/problem+json;charset=UTF-8");
            response.getWriter().write("{\"title\":\"Muitas tentativas\",\"status\":429}");
            return;
        }
        chain.doFilter(request, response);
    }
}
