package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.config.WebApplicationProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

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
            var properties = context.getBean(WebApplicationProperties.class);
            assertThat(properties.security().emailCooldown()).isEqualTo(Duration.ofSeconds(30));
            assertThat(properties.mail().retryDelay()).isEqualTo(Duration.ofSeconds(30));
            assertThat(properties.teacher().requiredDocuments()).containsExactly("comprovante-de-qualificacao");
        });
    }

    @Test void invalidSettingsFailAtStartup() {
        for (String property : new String[]{"app.security.rate-limit.capacity=0", "app.security.rate-limit.refill-period=0s",
                "app.security.rate-limit.namespace=", "app.public-base-url=invalid", "app.cors.allowed-origin=invalid"}) {
            runner.withPropertyValues(property).run(context -> assertThat(context).as(property).hasFailed());
        }
    }

    @Test void invalidMailRetryDelayFailsAtStartup() {
        for (String delay : new String[]{"-1s", "0s", "500ms"}) {
            runner.withPropertyValues("app.mail.retry-delay=" + delay)
                    .run(context -> assertThat(context).hasFailed()
                            .getFailure().hasStackTraceContaining("mail.retryDelay"));
        }
    }

    @Test void typedSettingsAcceptBoundariesAndNormalizeDocumentTypes() {
        for (String cooldown : new String[]{"0s", "5m"}) {
            runner.withPropertyValues("app.security.email-cooldown=" + cooldown, "app.mail.retry-delay=1s",
                    "app.teacher.required-documents= diploma, ,certificado,diploma ").run(context -> {
                assertThat(context).hasNotFailed();
                var properties = context.getBean(WebApplicationProperties.class);
                assertThat(properties.security().emailCooldown()).isEqualTo(cooldown.equals("0s") ? Duration.ZERO : Duration.ofMinutes(5));
                assertThat(properties.mail().retryDelay()).isEqualTo(Duration.ofSeconds(1));
                assertThat(properties.teacher().requiredDocuments()).containsExactly("diploma", "certificado");
            });
        }
    }

    @Test void invalidCooldownAndRequiredDocumentsFailAtStartup() {
        for (String property : new String[]{"app.security.email-cooldown=-1s", "app.security.email-cooldown=301s",
                "app.security.email-cooldown=invalid",
                "app.teacher.required-documents=", "app.teacher.required-documents=a,b,c,d,e",
                "app.teacher.required-documents=documento invalido"}) {
            runner.withPropertyValues(property).run(context -> assertThat(context).as(property).hasFailed());
        }
    }

    @Test void emptyDurationsUseTheBindingDefaults() {
        runner.withPropertyValues("app.security.email-cooldown=", "app.mail.retry-delay=").run(context -> {
            assertThat(context).hasNotFailed();
            var properties = context.getBean(WebApplicationProperties.class);
            assertThat(properties.security().emailCooldown()).isEqualTo(Duration.ofSeconds(30));
            assertThat(properties.mail().retryDelay()).isEqualTo(Duration.ofSeconds(30));
        });
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
