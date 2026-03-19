package ru.yandex.practicum.handlers.sensors;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaClientProducer;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.TimestampMapper;

@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {

    @Value(value = "${topics.sensors-events}")
    private String topicSensor;
    private final KafkaClientProducer kafkaClientProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        kafkaClientProducer.getProducer().send(new ProducerRecord<>(topicSensor, mapToAvro(event)));
    }

    private SensorEventAvro mapToAvro(SensorEventProto event) {
        MotionSensorProto sensorProto = event.getMotionSensorEvent();
        MotionSensorAvro sensorAvro = MotionSensorAvro.newBuilder()
                .setLinkQuality(sensorProto.getLinkQuality())
                .setMotion(sensorProto.getMotion())
                .setVoltage(sensorProto.getVoltage())
                .build();
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(TimestampMapper.mapToInstant(event.getTimestamp()))
                .setPayload(sensorAvro)
                .build();
    }
}