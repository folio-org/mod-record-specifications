package org.folio.support;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.KafkaUtils.createAndStartTestConsumer;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.folio.support.TestConstants.specificationUpdatedTopic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.extension.EnableKafka;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@EnableKafka
@Import(SpecificationITBase.IntegrationTestConfiguration.class)
public class SpecificationITBase extends IntegrationTestBase {

  protected KafkaMessageListenerContainer<String, SpecificationUpdatedEvent> container;
  protected BlockingQueue<ConsumerRecord<String, SpecificationUpdatedEvent>> consumerRecords;
  protected @Autowired KafkaTemplate<String, Object> kafkaTemplate;

  @BeforeEach
  void setUpKafka(@Autowired KafkaProperties kafkaProperties) {
    consumerRecords = new LinkedBlockingQueue<>();
    container =
      createAndStartTestConsumer(specificationUpdatedTopic(),
        consumerRecords, kafkaProperties, SpecificationUpdatedEvent.class);
  }

  @AfterEach
  void tearDownKafka() {
    consumerRecords.clear();
    container.stop();
  }

  private static List<RecordHeader> defaultKafkaHeaders() {
    return List.of(
      new RecordHeader(XOkapiHeaders.TENANT, TENANT_ID.getBytes()),
      new RecordHeader(XOkapiHeaders.USER_ID, USER_ID.getBytes()));
  }

  protected void assertSpecificationUpdatedEvents(int count) {
    for (int i = 0; i < count; i++) {
      assertSpecificationUpdatedEvent();
    }
  }

  protected void assertSpecificationUpdatedEvent() {
    var consumerRecord = getConsumedEvent();
    assertThat(consumerRecord.headers().toArray()).containsAll(defaultKafkaHeaders());

    var event = consumerRecord.value();
    assertThat(event.specificationId()).isEqualTo(BIBLIOGRAPHIC_SPECIFICATION_ID);
    assertThat(event.tenantId()).isEqualTo(TENANT_ID);
  }

  @SneakyThrows
  protected ConsumerRecord<String, SpecificationUpdatedEvent> getConsumedEvent() {
    var consumerRecord = consumerRecords.poll(5, TimeUnit.SECONDS);
    assertThat(consumerRecord).withFailMessage("Specification update event wasn't received").isNotNull();
    return consumerRecord;
  }

  @TestConfiguration
  public static class IntegrationTestConfiguration {

    @Bean
    public KafkaTemplate<String, Object> resourceKafkaTemplate(KafkaProperties kafkaProperties) {
      return new KafkaTemplate<>(producerFactory(kafkaProperties));
    }

    protected static ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
      Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties());
      configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
      return new DefaultKafkaProducerFactory<>(configProps);
    }
  }
}
