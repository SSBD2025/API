package pl.lodz.p.it.ssbd2025.ssbd02.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("ssbd")
            .withUsername("ssbd02admin")
            .withPassword("magn0lia");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.admin.username", postgres::getUsername);
        registry.add("spring.datasource.admin.password", postgres::getPassword);

        registry.add("spring.datasource.mok.username", () -> "ssbd02mok");
        registry.add("spring.datasource.mok.password", () -> "P@ssw0rd");
        registry.add("spring.datasource.mod.username", () -> "ssbd02mod");
        registry.add("spring.datasource.mod.password", () -> "P@ssw0rd");
    }
}
