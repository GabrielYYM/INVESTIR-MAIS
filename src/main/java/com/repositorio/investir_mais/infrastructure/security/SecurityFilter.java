package com.repositorio.investir_mais.infrastructure.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.repositorio.investir_mais.common.constants.LogMessageConstants;
import com.repositorio.investir_mais.common.constants.MessageConstants;
import com.repositorio.investir_mais.domain.auth.token.service.TokenBlackListService;
import com.repositorio.investir_mais.domain.auth.token.service.TokenService;
import com.repositorio.investir_mais.domain.user.service.interfaces.UserQueryService;
import com.repositorio.investir_mais.infrastructure.security.util.ClientIp;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserQueryService userService;
    private final TokenBlackListService invalidatedTokenService;


    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = this.recoverToken(request);

        if (token != null && !invalidatedTokenService.isBlacklisted(token)) {
            authenticateClient(token, request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateClient(@NonNull String token, @NonNull HttpServletRequest request) {
        try {
            String subjectId = tokenService.validateToken(token);

            UserDetails userDetails = userService.loadUserDetailsById(subjectId);

            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            MDC.put(MdcLoggingFilter.USER_ID_KEY, userDetails.getUsername());
        } catch (Exception e) {
            MDC.remove(MdcLoggingFilter.USER_ID_KEY);
            String ip = ClientIp.getClientIp(request);
            log.warn(LogMessageConstants.SECURITY.JWT_VALIDATION_FAILED,
                    ip, request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private String recoverToken(@NonNull HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(MessageConstants.Auth.BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(MessageConstants.Auth.BEARER_PREFIX.length());
    }
}
