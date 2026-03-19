package ru.yandex.practicum.handlers.hubs;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaClientProducer;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.mapper.TimestampMapper;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    @Value(value = "${topics.hubs-events}")
    private String topicHub;
    private final KafkaClientProducer kafkaClientProducer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        kafkaClientProducer.getProducer().send(new ProducerRecord<>(topicHub, mapToAvro(event)));
    }

    private HubEventAvro mapToAvro(HubEventProto event) {
        DeviceAddedEventProto eventProto = event.getDeviceAdded();
        DeviceAddedEventAvro eventAvro = DeviceAddedEventAvro.newBuilder()
                .setId(eventProto.getId())
                .setType(DeviceTypeAvro.valueOf(eventProto.getType().name()))
                .build();
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(TimestampMapper.mapToInstant(event.getTimestamp()))
                .setPayload(eventAvro)
                .build();
    }
}
