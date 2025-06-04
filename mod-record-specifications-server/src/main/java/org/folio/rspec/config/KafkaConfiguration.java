package org.folio.rspec.config;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Responsible for Kafka configuration.
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

  private final ObjectMapper objectMapper;

  /**
   * Creates and configures {@link org.springframework.kafka.core.ProducerFactory} as Spring bean.
   *
   * <p>Key type - {@link String}, value - {@link SpecificationUpdatedEvent}.</p>
   *
   * @return typed {@link org.springframework.kafka.core.ProducerFactory} object as Spring bean.
   */
  @Bean
  public ProducerFactory<String, SpecificationUpdatedEvent> producerFactory(KafkaProperties kafkaProperties) {
    return getProducerConfigProps(kafkaProperties);
  }

  /**
   * Creates and configures {@link org.springframework.kafka.core.KafkaTemplate} as Spring bean.
   *
   * <p>Key type - {@link String}, value - {@link SpecificationUpdatedEvent}.</p>
   *
   * @return typed {@link org.springframework.kafka.core.KafkaTemplate} object as Spring bean.
   */
  @Bean
  public KafkaTemplate<String, SpecificationUpdatedEvent> specificationChangeChangeKafkaTemplate(
    ProducerFactory<String, SpecificationUpdatedEvent> factory) {
    return new KafkaTemplate<>(factory);
  }

  @Bean
  public ConsumerFactory<String, UpdateRequestEvent> consumerFactory(
    KafkaProperties kafkaProperties,
    @Value("#{folioKafkaProperties.listener['update-requests'].autoOffsetReset}")
    OffsetResetStrategy autoOffsetReset) {
    var config = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    config.put(AUTO_OFFSET_RESET_CONFIG, autoOffsetReset.toString());
    return new DefaultKafkaConsumerFactory<>(config,
      new StringDeserializer(),
      new JsonDeserializer<>(UpdateRequestEvent.class));
  }

  @Bean("updateRequestEventListenerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, UpdateRequestEvent> listenerFactory(
    ConsumerFactory<String, UpdateRequestEvent> factory) {
    var listenerFactory = new ConcurrentKafkaListenerContainerFactory<String, UpdateRequestEvent>();
    listenerFactory.setConsumerFactory(factory);
    listenerFactory.setCommonErrorHandler(listenerErrorHandler());
    return listenerFactory;
  }

  private @NotNull DefaultErrorHandler listenerErrorHandler() {
    return new DefaultErrorHandler(
      (thrownException, event) -> log.error("Error in event processing [exception={}, event={}",
        thrownException, event),
      new FixedBackOff(2000L, 3L));
  }

  private <T> ProducerFactory<String, T> getProducerConfigProps(KafkaProperties kafkaProperties) {
    return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null),
      new StringSerializer(), new JsonSerializer<>(objectMapper));
  }
}
