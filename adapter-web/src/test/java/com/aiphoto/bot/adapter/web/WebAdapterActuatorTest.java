package com.aiphoto.bot.adapter.web;

import com.aiphoto.bot.adapter.web.config.WebAdapterConfig;
import com.aiphoto.bot.core.domain.Preset;
import com.aiphoto.bot.core.port.persistence.PresetRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest(
        classes = {WebAdapterActuatorTest.TestApp.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class WebAdapterActuatorTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({WebAdapterConfig.class, TestPresetConfig.class})
    static class TestApp { }

    @Autowired
    private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

    @Test
    void actuatorHealthEndpointWorks() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    static class TestPresetConfig {
        @Bean
        PresetRepository presetRepository() {
            return new PresetRepository() {
                @Override public Optional<Preset> findById(UUID id) { return Optional.empty(); }
                @Override public Optional<Preset> findByName(String name) { return Optional.empty(); }
                @Override public List<Preset> findAll() {
                    return List.of(new Preset(UUID.randomUUID(), "Test", "model", Map.of(), Instant.now()));
                }
            };
        }
    }
}
