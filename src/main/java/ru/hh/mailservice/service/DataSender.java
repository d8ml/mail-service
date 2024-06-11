package ru.hh.mailservice.service;

import ru.hh.mailservice.model.StringValue;

public interface DataSender {
    void send(StringValue value);
}
