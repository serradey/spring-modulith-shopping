package com.example.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Verifies that the application respects its module boundaries (no cycles, no illegal access). */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(ShoppingApplication.class);

    @Test
    void verifiesModuleStructure() {
        modules.verify();
    }
}
