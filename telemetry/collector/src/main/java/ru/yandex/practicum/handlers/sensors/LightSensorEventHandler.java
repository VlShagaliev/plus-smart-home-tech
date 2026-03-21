package ru.yandex.practicum.handlers.sensors;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaClientProducer;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.TimestampMapper;

@Component
@RequiredArgsConstructor
public class LightSensorEventHandler implements SensorEventHandler {
    @Value(value = "${topics.sensors-events}")
    private String topicSensor;

    private final KafkaClientProducer kafkaClientProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        kafkaClientProducer.getProducer().send(new ProducerRecord<>(topicSensor, mapToAvro(event)));
    }

    private SensorEventAvro mapToAvro(SensorEventProto event) {
        LightSensorProto sensorProto = event.getLightSensorEvent();
        LightSensorAvro sensorAvro = LightSensorAvro.newBuilder()
                .setLinkQuality(sensorProto.getLinkQuality())
                .setLuminosity(sensorProto.getLuminosity())
                .build();
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(TimestampMapper.mapToInstant(event.getTimestamp()))
                .setPayload(sensorAvro)
                .build();
    }
}
