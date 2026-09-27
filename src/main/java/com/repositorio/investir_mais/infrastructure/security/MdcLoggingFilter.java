package com.repositorio.investir_mais.infrastructure.security;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.repositorio.investir_mais.infrastructure.security.util.ClientIp;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro responsável por inicializar o MDC (Mapped Diagnostic Context) com
 * Correlation ID (traceId) e IP do cliente em todas as requisições HTTP.
 * Também mede a duração da requisição e registra logs estruturados de entrada e saída.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String CLIENT_IP_KEY = "clientIp";
    public static final String USER_ID_KEY = "userId";
    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static/") || path.startsWith("/assets/") ||
               path.startsWith("/css/") || path.startsWith("/js/") ||
               path.endsWith(".ico") || path.endsWith(".png") || path.endsWith(".svg");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String traceId = resolveTraceId(request);
        String clientIp = ClientIp.getClientIp(request);

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(CLIENT_IP_KEY, clientIp);
        response.setHeader(TRACE_HEADER, traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = (queryString != null && !queryString.isBlank()) ? uri + "?" + queryString : uri;

        log.info("HTTP {} {} iniciado | IP: {}", method, fullPath, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            enrichMdcWithUser();
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            log.info("HTTP {} {} concluído | Status: {} | Duração: {}ms",
                    method, fullPath, status, duration);

            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeader(CORRELATION_HEADER);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    private void enrichMdcWithUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            MDC.put(USER_ID_KEY, auth.getName());
        }
    }
}
