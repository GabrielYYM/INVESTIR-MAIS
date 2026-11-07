package com.repositorio.investir_mais;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.repositorio.investir_mais.infrastructure.PostgreSqlIntegrationTest;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class InvestirMaisApplicationTests extends PostgreSqlIntegrationTest {

	@Test
	void contextLoads() {
	}

}
