package ru.hh.mailservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.mailservice.model.StringValue;

import java.util.List;

public class StringValueConsumerLogger implements StringValueConsumer {
    private static final Logger log = LoggerFactory.getLogger(StringValueConsumerLogger.class);

    @Override
    public void accept(List<StringValue> values) {
        for (StringValue value : values) {
            log.info("сообщение: {}", value);
        }
    }
}