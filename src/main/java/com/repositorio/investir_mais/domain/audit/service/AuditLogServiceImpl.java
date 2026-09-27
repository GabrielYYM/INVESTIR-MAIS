package com.repositorio.investir_mais.domain.audit.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.repositorio.investir_mais.domain.audit.model.AuditLog;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditAction;
import com.repositorio.investir_mais.domain.audit.model.enums.AuditStatus;
import com.repositorio.investir_mais.domain.audit.repository.AuditLogRepository;
import com.repositorio.investir_mais.domain.audit.service.interfaces.AuditLogService;
import com.repositorio.investir_mais.infrastructure.security.MdcLoggingFilter;
import com.repositorio.investir_mais.infrastructure.security.UserDetailsImpl;
import com.repositorio.investir_mais.infrastructure.security.util.ClientIp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT_LOGGER");

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String resourceType, String resourceId, AuditStatus status, String details) {
        ActorInfo actor = resolveCurrentActor();
        String ip = resolveCurrentIp();
        String traceId = resolveTraceId();

        persistAndLog(action, actor.id(), actor.email(), resourceType, resourceId, ip, traceId, status, details);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String actorId, String actorEmail, String resourceType, String resourceId, String ipAddress, AuditStatus status, String details) {
        String traceId = resolveTraceId();
        String resolvedIp = (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : resolveCurrentIp();

        persistAndLog(action, actorId, actorEmail, resourceType, resourceId, resolvedIp, traceId, status, details);
    }

    private void persistAndLog(AuditAction action, String actorId, String actorEmail,
                               String resourceType, String resourceId, String ipAddress,
                               String traceId, AuditStatus status, String details) {
        try {
            AuditLog auditEntry = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .traceId(traceId)
                    .actorId(actorId)
                    .actorEmail(actorEmail)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .ipAddress(ipAddress)
                    .status(status)
                    .details(details)
                    .build();

            auditLogRepository.save(auditEntry);

            AUDIT_LOGGER.info("AUDIT | Action: {} | Status: {} | Actor: {} ({}) | Resource: {}:{} | IP: {} | Details: {}",
                    action, status, actorEmail, actorId, resourceType, resourceId, ipAddress, details);

        } catch (Exception ex) {
            log.error("Falha ao registrar log de auditoria no banco de dados para a ação {}: {}", action, ex.getMessage(), ex);
        }
    }

    private record ActorInfo(String id, String email) {}

    private ActorInfo resolveCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            String id = null;
            if (auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
                if (userDetails.getUser() != null && userDetails.getUser().getId() != null) {
                    id = userDetails.getUser().getId().toString();
                }
            }
            return new ActorInfo(id != null ? id : email, email);
        }
        return new ActorInfo("SYSTEM", "SYSTEM");
    }

    private String resolveCurrentIp() {
        String mdcIp = MDC.get(MdcLoggingFilter.CLIENT_IP_KEY);
        if (mdcIp != null && !mdcIp.isBlank()) {
            return mdcIp;
        }
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return ClientIp.getClientIp(attrs.getRequest());
            }
        } catch (Exception ignored) {
        }
        return "N/A";
    }

    private String resolveTraceId() {
        String traceId = MDC.get(MdcLoggingFilter.TRACE_ID_KEY);
        return (traceId != null && !traceId.isBlank()) ? traceId : UUID.randomUUID().toString();
    }
}
