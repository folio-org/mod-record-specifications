package org.folio.rspec.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Responsible for Kafka configuration.
 */
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

  private <T> ProducerFactory<String, T> getProducerConfigProps(KafkaProperties kafkaProperties) {
    return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null),
      new StringSerializer(), new JsonSerializer<>(objectMapper));
  }
}
