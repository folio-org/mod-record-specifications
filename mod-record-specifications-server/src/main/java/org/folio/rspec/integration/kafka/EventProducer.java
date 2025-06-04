package org.folio.rspec.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.tools.kafka.KafkaUtils;
import org.springframework.kafka.core.KafkaTemplate;

@Log4j2
@RequiredArgsConstructor
public abstract class EventProducer<T, D> {

  private final KafkaTemplate<String, D> template;
  private final FolioExecutionContext context;

  public void sendEvent(T data) {
    log.info("sendEvent::topic={}", topicName());
    var messageBody = buildEvent(data);
    var producerRecord = toProducerRecord(messageBody);
    template.send(producerRecord);
  }

  private ProducerRecord<String, D> toProducerRecord(D messageBody) {
    var producerRecord = new ProducerRecord<String, D>(tenantTopicName(), messageBody);

    KafkaUtils.toKafkaHeaders(context.getOkapiHeaders())
      .forEach(header -> producerRecord.headers().add(header));

    return producerRecord;
  }

  private String tenantTopicName() {
    return KafkaUtils.getTenantTopicName(topicName(), tenantId());
  }

  protected String tenantId() {
    return context.getTenantId();
  }

  protected abstract String topicName();

  protected abstract D buildEvent(T data);
}
