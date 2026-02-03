package ru.yandex.practicum.kafka;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

import java.util.Properties;

@Configuration
public class KafkaProducerConfig {
    @Value(value = "${kafka.bootstrap-servers}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    @Bean
    KafkaClientProducer getProducer() {
        return new KafkaClientProducer() {
            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    initProducer();
                }
                return producer;
            }

            private void initProducer() {
                Properties config = new Properties();
                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

                producer = new KafkaProducer<>(config);
            }

            @PreDestroy
            @Override
            public void stop() {
                if (producer != null) {
                    producer.close();
                }
            }
        };
    }
}
