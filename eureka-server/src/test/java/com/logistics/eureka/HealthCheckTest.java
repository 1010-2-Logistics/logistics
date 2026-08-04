package com.logistics.eureka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckTest {

    @LocalServerPort
    int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName("Health Endpoint는 UP을 반환한다.")
    void healthCheck() {

        String response =
                restTemplate.getForObject(
                        "http://localhost:" + port + "/actuator/health",
                        String.class
                );

        assertThat(response)
                .contains("UP");
    }
}