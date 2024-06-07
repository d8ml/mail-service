package ru.hh.mailservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.mailservice.model.StringValue;
import ru.hh.mailservice.service.EmailService;
import ru.hh.mailservice.service.StringValueConsumer;

import java.util.List;

public class StringValueConsumerEmailSender implements StringValueConsumer {
    private static final Logger log = LoggerFactory.getLogger(StringValueConsumerEmailSender.class);
    private static final String ADDRESS_TO = "d8ml@yandex.ru";

    private final EmailService emailService;

    public StringValueConsumerEmailSender(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void accept(List<StringValue> values) {
        for (StringValue value : values) {
            log.info("сообщение: {}", value);
            emailService.sendSimpleEmail(
                    ADDRESS_TO,
                    String.valueOf(value.id()),
                    value.value()
            );
        }
    }
}
