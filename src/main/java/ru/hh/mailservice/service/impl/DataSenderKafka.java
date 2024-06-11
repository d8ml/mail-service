package ru.hh.mailservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import ru.hh.mailservice.model.StringValue;
import ru.hh.mailservice.service.DataSender;

import java.util.function.Consumer;


public class DataSenderKafka implements DataSender {
    private static final Logger log = LoggerFactory.getLogger(DataSenderKafka.class);

    private final KafkaTemplate<String, StringValue> template;

    private final Consumer<StringValue> consumer;

    private final String topic;

    public DataSenderKafka(
            String topic,
            KafkaTemplate<String, StringValue> template,
            Consumer<StringValue> consumer) {
        this.topic = topic;
        this.template = template;
        this.consumer = consumer;
    }

    @Override
    public void send(StringValue value) {
        try {
            log.info("Sending: {}", value);
            template.send(topic, value)
                    .whenComplete(
                            (result, e) -> {
                                if (e == null) {
                                    log.info("сообщение с id: {} успешно отправлено, offset: {}",
                                            value.id(),
                                            result.getRecordMetadata().offset()
                                    );
                                    consumer.accept(value);
                                } else {
                                    log.error("сообщение с id: {} не отправлено", value.id(), e);
                                }
                            });

        } catch (Exception e) {
            log.error("ошибка отправки, сообщение: {}", value, e);
        }
    }
}
