package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class LegalAcceptanceConfiguration implements WebMvcConfigurer {
    private final LegalDocumentsService legal;
    private static final Set<String> PUBLIC_ACCOUNT_ROUTES = Set.of("/api/users/register", "/api/users/verify-email",
            "/api/users/resend-verification", "/api/users/forgot-password", "/api/users/reset-password");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

                String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                if (pattern == null || pattern.startsWith("/api/legal/") || PUBLIC_ACCOUNT_ROUTES.contains(pattern)
                        || request.getUserPrincipal() == null) return true;

                if (("/api/users/{id}".equals(pattern) && Set.of("GET", "PUT", "DELETE").contains(request.getMethod()))
                        || "/api/users/{id}/erasure".equals(pattern) || "/api/users/update-password".equals(pattern)) return true;
                if (legal.status(request.getUserPrincipal().getName()).acceptanceRequired()) {
                    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_REQUIRED,
                            "Leia e confirme a versão atual dos Termos de Uso e da Política de Privacidade para continuar.");
                    problem.setType(URI.create("urn:investir-mais:legal-acceptance-required"));
                    problem.setProperty("code", "LEGAL_ACCEPTANCE_REQUIRED");
                    throw new ErrorResponseException(HttpStatus.PRECONDITION_REQUIRED, problem, null);
                }
                return true;
            }
        }).addPathPatterns("/api/**");
    }
}
