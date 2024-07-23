package org.folio.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.KafkaUtils.createAndStartTestConsumer;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.folio.support.TestConstants.specificationUpdatedTopic;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.extension.EnableKafka;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

@EnableKafka
public class SpecificationITBase extends IntegrationTestBase {

  protected KafkaMessageListenerContainer<String, SpecificationUpdatedEvent> container;
  protected BlockingQueue<ConsumerRecord<String, SpecificationUpdatedEvent>> consumerRecords;

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

}
