package com.sbm.sevenrooms.sevenroomshandler.config;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfiguration {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.security.protocol}")
    private String protocolConfig;

    @Value(value = "${spring.kafka.sasl.jaas.config}")
    private String saslJaasConfig;

    @Value(value = "${spring.kafka.sasl.mechanism}")
    private String saslMechanism;

    @Value(value = "${spring.kafka.ssl.sslTruststoreLocation}")
    private String trustStoreLocation;

    private String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        if (bootstrapAddress != null) {
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        }
        if (protocolConfig != null) {
            configProps.put(SECURITY_PROTOCOL_CONFIG, protocolConfig);
        }
        if (saslJaasConfig != null) {
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        }
        if (saslMechanism != null) {
            configProps.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
        }
        if (trustStoreLocation != null) {
            configProps.put("ssl.truststore.location", trustStoreLocation);
        }
        if (trustStorePassword != null) {
            configProps.put("ssl.truststore.password", trustStorePassword);
        }
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(16 * 1024)); // 32 KB batch size
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
