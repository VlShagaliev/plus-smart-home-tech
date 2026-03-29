package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.AggregatorConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final AggregatorConfig aggregatorConfig;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    private static void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int count,
                                      KafkaConsumer<String, SensorEventAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    public void start() {
        try {
            consumer.subscribe(aggregatorConfig.getSensorTopic());
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(aggregatorConfig.getConsumeAttemptTimeout());
                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    Optional<SensorsSnapshotAvro> sensorsSnapshotAvroOpt = updateState(record.value());
                    if (sensorsSnapshotAvroOpt.isPresent()) {
                        SensorsSnapshotAvro snapshotAvro = sensorsSnapshotAvroOpt.get();
                        producer.send(new ProducerRecord<>(aggregatorConfig.getSnapshotTopic(), snapshotAvro));
                        log.info("Snapshot from hub ID = {} send to topic: {}", snapshotAvro.getHubId(),
                                aggregatorConfig.getSnapshotTopic());
                    }
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {

        } catch (
                Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }

    }

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot;
        if (snapshots.containsKey(event.getHubId())) {
            snapshot = snapshots.get(event.getHubId());
        } else {
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setSensorsState(new HashMap<>())
                    .setTimestamp(Instant.now())
                    .build();
            snapshots.put(event.getHubId(), snapshot);
        }
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        if (sensorsState.containsKey(event.getId())) {
            if (isDataNotChanged(sensorsState.get(event.getId()), event)) {
                return Optional.empty();
            }
        }
        SensorStateAvro sensorStateAvro = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
        sensorsState.put(event.getId(), sensorStateAvro);
        snapshot.setSensorsState(sensorsState);
        snapshot.setTimestamp(event.getTimestamp());
        return Optional.of(snapshot);
    }


    private boolean isDataNotChanged(SensorStateAvro oldState, SensorEventAvro event) {
        return (oldState.getTimestamp().isAfter(event.getTimestamp())) ||
                (oldState.getData().equals(event.getPayload()));
    }

    public void stop() {
        consumer.wakeup();
    }
}