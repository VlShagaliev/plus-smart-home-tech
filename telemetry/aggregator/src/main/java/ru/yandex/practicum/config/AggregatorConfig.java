package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Getter
@Setter
@Configuration
@ConfigurationProperties("aggregator")
public class AggregatorConfig {
    private String snapshotTopic;
    private List<String> sensorTopic;
    private Duration consumeAttemptTimeout;
    private Properties producerProperties;
    private Properties consumerProperties;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> producer() {
        return new KafkaProducer<>(getProducerProperties());
    }

    @Bean
    public KafkaConsumer<String, SensorEventAvro> consumer() {
        return new KafkaConsumer<>(getConsumerProperties());
    }
}