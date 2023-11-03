package com.example.aichatprojectdat.integration;

import org.junit.jupiter.api.Order;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Order(1)
@Testcontainers // Indicates that Testcontainers should be used
public abstract class AbstractIntegrationTest {

    // Define your MySQL container (it will be shared between test classes)
    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:latest"));

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", AbstractIntegrationTest::r2dbcUrl);
        registry.add("spring.r2dbc.username", mySQLContainer::getUsername);
        registry.add("spring.r2dbc.password", mySQLContainer::getPassword);
        registry.add("spring.flyway.url", mySQLContainer::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:mysql://%s:%s/%s",
                mySQLContainer.getHost(),
                mySQLContainer.getMappedPort(MySQLContainer.MYSQL_PORT),
                mySQLContainer.getDatabaseName());
    }
}
