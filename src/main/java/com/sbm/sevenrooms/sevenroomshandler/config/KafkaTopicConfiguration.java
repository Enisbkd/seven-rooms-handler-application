package com.sbm.sevenrooms.sevenroomshandler.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@Profile("local")
public class KafkaTopicConfiguration {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.topics.client-topic}")
    private String clientsTopic;

    @Value(value = "${spring.kafka.topics.reservation-topic}")
    private String reservationsTopic;

    @Value(value = "${spring.kafka.topics.handler-deadletters-topic}")
    private String gtwDeadLettersTopic;

    @Value(value = "${spring.kafka.topics.handler-logs-topic}")
    private String gtwlogsTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic clientTopic() {
        return new NewTopic(clientsTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic reservationTopic() {
        return new NewTopic(reservationsTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic gtwDeadLetterTopic() {
        return new NewTopic(gtwDeadLettersTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic gtwlogsTopic() {
        return new NewTopic(gtwlogsTopic, 3, (short) 1);
    }
}
