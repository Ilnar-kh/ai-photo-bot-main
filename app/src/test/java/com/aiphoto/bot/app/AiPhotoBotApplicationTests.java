//package com.aiphoto.bot.app;
//
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.context.ApplicationContextInitializer;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@Testcontainers
//@ContextConfiguration(initializers = AiPhotoBotApplicationTests.TestContainersInitializer.class)
//class AiPhotoBotApplicationTests {
//
//    static PostgreSQLContainer<?> postgres =
//            new PostgreSQLContainer<>("postgres:16.3-alpine");
//
//    static class TestContainersInitializer
//            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//
//        @Override
//        public void initialize(ConfigurableApplicationContext context) {
//            postgres.start();
//
//            System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
//            System.setProperty("spring.datasource.username", postgres.getUsername());
//            System.setProperty("spring.datasource.password", postgres.getPassword());
//            System.setProperty("spring.jpa.hibernate.ddl-auto", "validate");
//            System.setProperty("spring.flyway.enabled", "true");
//        }
//    }
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Test
//    void contextLoadsAndExposesActuator() {
//        String body = restTemplate.getForObject("/actuator/health", String.class);
//        Assertions.assertThat(body).contains("UP");
//    }
//}