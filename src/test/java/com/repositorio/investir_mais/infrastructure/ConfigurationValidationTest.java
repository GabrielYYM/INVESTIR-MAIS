package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationValidationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class)).withUserConfiguration(Config.class);

    @Test void defaultsAreBoundAndValid() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(AuthenticationRateLimitProperties.class)
                    .hasSingleBean(WebApplicationProperties.class)
                    .hasSingleBean(BrapiProperties.class);
            assertThat(context.getBean(AuthenticationRateLimitProperties.class).capacity()).isEqualTo(10);
        });
    }

    @Test void invalidSettingsFailAtStartup() {
        for (String property : new String[]{"app.security.rate-limit.capacity=0", "app.security.rate-limit.refill-period=0s",
                "app.security.rate-limit.namespace=", "app.public-base-url=invalid", "app.cors.allowed-origin=invalid"}) {
            runner.withPropertyValues(property).run(context -> assertThat(context).hasFailed());
        }
    }

    @Test void invalidMailRetryDelayFailsAtStartup() {
        for (String delay : new String[]{"-1s", "0s", "500ms"}) {
            runner.withUserConfiguration(AuthenticationEmailHandler.class)
                    .withBean(org.springframework.mail.javamail.JavaMailSender.class,
                            () -> org.mockito.Mockito.mock(org.springframework.mail.javamail.JavaMailSender.class))
                    .withBean(com.repositorio.investir_mais.domain.AccountSecurityService.class,
                            () -> org.mockito.Mockito.mock(com.repositorio.investir_mais.domain.AccountSecurityService.class))
                    .withBean(org.springframework.modulith.events.FailedEventPublications.class,
                            () -> org.mockito.Mockito.mock(org.springframework.modulith.events.FailedEventPublications.class))
                    .withPropertyValues("app.mail.retry-delay=" + delay)
                    .run(context -> assertThat(context).hasFailed()
                            .getFailure().hasRootCauseMessage("app.mail.retry-delay deve ser de pelo menos 1 segundo"));
        }
    }

    @Configuration
    @ConfigurationPropertiesScan(basePackageClasses = AuthenticationRateLimitProperties.class)
    static class Config {}
}
