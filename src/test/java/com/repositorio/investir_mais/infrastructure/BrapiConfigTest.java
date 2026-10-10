package com.repositorio.investir_mais.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {ClientConfig.class, RestClientAutoConfiguration.class})
@ActiveProfiles("test")
class ClientConfigTest {

    @Autowired
    private AssetQuoteClient assetQuoteClient;

    @Test
    void assetQuoteClient_ShouldBeInstantiatedAndInjected() {
        assertNotNull(assetQuoteClient);
    }
}