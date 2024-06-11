package ru.hh.mailservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import ru.hh.mailservice.model.StringValue;
import ru.hh.mailservice.service.EmailService;
import ru.hh.mailservice.service.StringValueConsumer;
import ru.hh.mailservice.service.impl.StringValueConsumerEmailSender;

import java.util.List;

import static org.springframework.kafka.support.serializer.JsonDeserializer.TYPE_MAPPINGS;

@Configuration
public class ConsumerConfiguration {
    private static final Integer PARTITIONS_COUNT = 1;
    private static final Integer REPLICAS_COUNT = 1;
    private static final Logger log = LoggerFactory.getLogger(ConsumerConfiguration.class);

    public final String topicName;

    public ConsumerConfiguration(@Value("${application.kafka.topic}") String topicName) {
        this.topicName = topicName;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.enhancedObjectMapper();
    }

    @Bean
    public ConsumerFactory<String, StringValue> consumerFactory(
            KafkaProperties kafkaProperties, ObjectMapper objectMapper) {
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(TYPE_MAPPINGS, "ru.hh.mailservice.model.StringValue:ru.hh.mailservice.model.StringValue");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 3_000);

        var kafkaConsumerFactory = new DefaultKafkaConsumerFactory<String, StringValue>(props);
        kafkaConsumerFactory.setValueDeserializer(new JsonDeserializer<>(objectMapper));
        return kafkaConsumerFactory;
    }

    @Bean("listenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, StringValue>>
            listenerContainerFactory(ConsumerFactory<String, StringValue> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, StringValue>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setIdleBetweenPolls(1_000);
        factory.getContainerProperties().setPollTimeout(3_000);

        var executor = new SimpleAsyncTaskExecutor("consumer-");
        executor.setConcurrencyLimit(5);
        var listenerTaskExecutor = new ConcurrentTaskExecutor(executor);
        factory.getContainerProperties().setListenerTaskExecutor(listenerTaskExecutor);
        return factory;
    }

//    @Bean
//    public NewTopic topic() {
//        return TopicBuilder
//                .name(topicName)
//                .partitions(PARTITIONS_COUNT)
//                .replicas(REPLICAS_COUNT)
//                .build();
//    }

    @Bean
    public StringValueConsumer stringValueConsumerEmailSender(EmailService emailService) {
        return new StringValueConsumerEmailSender(emailService);
    }

    @Bean
    public KafkaClient stringValueConsumer(StringValueConsumer stringValueConsumer) {
        return new KafkaClient(stringValueConsumer);
    }

    public static class KafkaClient {
        private final StringValueConsumer stringValueConsumer;

        public KafkaClient(StringValueConsumer stringValueConsumer) {
            this.stringValueConsumer = stringValueConsumer;
        }

        @KafkaListener(
                topics = "${application.kafka.topic}",
                containerFactory = "listenerContainerFactory")
        public void listen(@Payload List<StringValue> values) {
            log.info("число сообщений: {}", values.size());
            stringValueConsumer.accept(values);
        }
    }
}
