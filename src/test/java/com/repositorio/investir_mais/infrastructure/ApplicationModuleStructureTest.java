package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.InvestirMaisApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApplicationModuleStructureTest {

    @Test
    void modulesShouldHaveNoCycles() {
        ApplicationModules.of(InvestirMaisApplication.class).verify();
    }
}
