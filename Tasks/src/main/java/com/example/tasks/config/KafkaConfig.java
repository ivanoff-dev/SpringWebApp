package com.example.tasks.config;

import com.example.tasks.dto.TaskDTO;

import com.example.tasks.dto.TaskStatusMessage;
import com.example.tasks.kafka.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
//@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap.servers}")
    private String servers;
    @Value("${kafka.consumer.group-id}")
    private String groupId;
    @Value("${kafka.session_timeout_ms:15000}")
    private String sessionTimeout;
    @Value("${kafka.max_partition.fetch.bytes:300000}")
    private String maxPartitionFetchBytes;
    @Value("${kafka.max.poll.records:1}")
    private String maxPollRecords;
    @Value("${kafka.max.poll.interval.ms:3000}")
    private String maxPollIntervalsMs;
    @Value("${kafka.topic.task_status_changed}")
    private String taskStatusTopic;

    @Bean
    public ConsumerFactory<String, TaskStatusMessage> consumerListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.tasks.dto.TaskStatusMessage");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(TaskStatusMessage.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TaskStatusMessage> kafkaListenerContainerFactory(
            @Qualifier("consumerListenerFactory") ConsumerFactory<String, TaskStatusMessage> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TaskStatusMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory,
                                    ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(false);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMicrometerEnabled(true);
        factory.setCommonErrorHandler(errorHandler());
    }

    // для повторной попытки прочитать сообщение
    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.error("RetryListeners message = {}, offset = {} deliveryAttempt = {}",
                        ex.getMessage(), record.offset(), deliveryAttempt)
        );
        return handler;
    }

    @Bean("producer_task_status")
    public KafkaTemplate<String, TaskStatusMessage> kafkaStatusTemplate(
            @Qualifier("producerStatusFactory") ProducerFactory<String, TaskStatusMessage> factory) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public KafkaProducerService producerTask(@Qualifier("producer_task_status") KafkaTemplate template) {
        template.setDefaultTopic(taskStatusTopic);
        return new KafkaProducerService(template);
    }

    @Bean
    public ProducerFactory<String, TaskStatusMessage> producerStatusFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }


}
