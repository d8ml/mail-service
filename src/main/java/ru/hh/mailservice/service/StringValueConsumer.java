package ru.hh.mailservice.service;

import ru.hh.mailservice.model.StringValue;

import java.util.List;

public interface StringValueConsumer {
    void accept(List<StringValue> value);
}
