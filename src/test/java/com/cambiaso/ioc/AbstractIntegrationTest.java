package com.cambiaso.ioc;

import com.cambiaso.ioc.config.GlobalTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(GlobalTestConfiguration.class)
@Transactional
public abstract class AbstractIntegrationTest {
}
