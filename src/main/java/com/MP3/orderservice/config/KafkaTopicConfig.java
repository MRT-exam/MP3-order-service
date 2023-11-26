package com.MP3.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${SPRING_KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapAddress;


    @Bean
    public NewTopic inventoryUpdateTopic() {
        return TopicBuilder.name("inventoryUpdateTopic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
