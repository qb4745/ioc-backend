package com.cambiaso.ioc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Context load smoke test (profile=test)")
public class ContextLoadSmokeTest {

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        // If the application context fails to start, this test will fail during startup.
    }
}

