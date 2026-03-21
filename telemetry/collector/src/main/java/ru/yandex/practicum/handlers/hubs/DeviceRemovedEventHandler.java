package ru.yandex.practicum.handlers.hubs;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaClientProducer;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.mapper.TimestampMapper;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    @Value(value = "${topics.hubs-events}")
    private String topicHub;
    private final KafkaClientProducer kafkaClientProducer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        kafkaClientProducer.getProducer().send(new ProducerRecord<>(topicHub, mapToAvro(event)));
    }

    private HubEventAvro mapToAvro(HubEventProto event) {
        DeviceRemovedEventProto eventProto = event.getDeviceRemoved();
        DeviceRemovedEventAvro eventAvro = DeviceRemovedEventAvro.newBuilder()
                .setId(eventProto.getId())
                .build();
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(TimestampMapper.mapToInstant(event.getTimestamp()))
                .setPayload(eventAvro)
                .build();
    }
}