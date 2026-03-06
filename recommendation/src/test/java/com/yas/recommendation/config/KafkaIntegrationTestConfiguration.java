package com.yas.recommendation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class KafkaIntegrationTestConfiguration {

    @Bean
    public KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.0.9"));
        kafkaContainer.start();
        return kafkaContainer;
    }

    @Bean
    public DynamicPropertyRegistrar kafkaProperties(KafkaContainer kafkaContainer) {
        return registry -> {
            registry.add("spring.kafka.bootstrap-servers",
                kafkaContainer::getBootstrapServers);
            registry.add("spring.kafka.consumer.bootstrap-servers",
                kafkaContainer::getBootstrapServers);
            registry.add("spring.kafka.producer.bootstrap-servers",
                kafkaContainer::getBootstrapServers);
            registry.add("spring.kafka.consumer.auto-offset-reset",
                () -> "earliest");
        };
    }

    @Bean
    public PostgreSQLContainer pgvectorContainer() {
        var image = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");
        var postgres = new PostgreSQLContainer<>(image);
        postgres.start();
        return postgres;
    }

    @Bean
    public DynamicPropertyRegistrar pgvectorProperties(PostgreSQLContainer pgvectorContainer) {
        return registry -> {
            registry.add("spring.datasource.url", pgvectorContainer::getJdbcUrl);
            registry.add("spring.datasource.username", pgvectorContainer::getUsername);
            registry.add("spring.datasource.password", pgvectorContainer::getPassword);
        };
    }
}