package com.repositorio.investir_mais.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@SpringBootTest(classes = {BrapiConfig.class, RestClientAutoConfiguration.class})
@ActiveProfiles("test")
class BrapiConfigTest {

    @Autowired
    private RestClient brapiClient;

    @Test
    void brapiClient_DeveSerInstanciadoEInjetado() {
        assertNotNull(brapiClient);
    }
}