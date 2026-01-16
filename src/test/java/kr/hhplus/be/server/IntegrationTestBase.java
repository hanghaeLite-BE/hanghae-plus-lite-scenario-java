package kr.hhplus.be.server;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {
}
