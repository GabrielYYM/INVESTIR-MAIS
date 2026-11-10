package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.infrastructure.config.WebApplicationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationValidationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class)).withUserConfiguration(Config.class);

    @Test void defaultsAreBoundAndValid() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(WebApplicationProperties.class);
            var rateLimit = context.getBean(WebApplicationProperties.class).security().rateLimit();
            assertThat(rateLimit.capacity()).isEqualTo(10);
            assertThat(rateLimit.refillPeriod()).isEqualTo(Duration.ofMinutes(1));
            assertThat(rateLimit.namespace()).isEqualTo("investir-mais:rate-limit");
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
                    .withInitializer(context -> context.getBeanFactory()
                            .setConversionService(ApplicationConversionService.getSharedInstance()))
                    .withBean(ResendEmailSender.class, () -> org.mockito.Mockito.mock(ResendEmailSender.class))
                    .withBean(com.repositorio.investir_mais.domain.AccountSecurityService.class,
                            () -> org.mockito.Mockito.mock(com.repositorio.investir_mais.domain.AccountSecurityService.class))
                    .withBean(org.springframework.modulith.events.FailedEventPublications.class,
                            () -> org.mockito.Mockito.mock(org.springframework.modulith.events.FailedEventPublications.class))
                    .withPropertyValues("app.mail.retry-delay=" + delay)
                    .run(context -> assertThat(context).hasFailed()
                            .getFailure().hasRootCauseMessage("app.mail.retry-delay deve ser de pelo menos 1 segundo"));
        }
    }

    @Test void resendRequiresApiKeyAndSender() {
        var mailRunner = runner.withUserConfiguration(ResendEmailSender.class)
                .withBean(RestClient.Builder.class, RestClient::builder);
        mailRunner.withPropertyValues("app.mail.resend.api-key=", "app.mail.resend.from=conta@example.test")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().hasRootCauseMessage("RESEND_API_KEY é obrigatório para enviar e-mails pelo Resend."));
        mailRunner.withPropertyValues("app.mail.resend.api-key=test-key", "app.mail.resend.from=")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().hasRootCauseMessage("RESEND_FROM é obrigatório para enviar e-mails pelo Resend."));
        mailRunner.withPropertyValues("app.mail.resend.api-key=test-key", "app.mail.resend.from=conta@example.test")
                .run(context -> assertThat(context).hasSingleBean(ResendEmailSender.class));
    }

    @Configuration
    @ConfigurationPropertiesScan(basePackageClasses = WebApplicationProperties.class)
    static class Config {}
}
