package org.folio.rspec.integration.kafka;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.FolioExecutionContext;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Primary
@Component
public class SpecificationPartialChangedEventProducer extends EventProducer<UUID, SpecificationUpdatedEvent> {

  public SpecificationPartialChangedEventProducer(
    KafkaTemplate<String, SpecificationUpdatedEvent> template,
    FolioExecutionContext context) {
    super(template, context);
  }

  @Override
  protected String topicName() {
    return KafkaTopicName.SPECIFICATION_UPDATED.getTopicName();
  }

  @Override
  protected SpecificationUpdatedEvent buildEvent(UUID specificationId) {
    return new SpecificationUpdatedEvent(specificationId, tenantId(), SpecificationUpdatedEvent.UpdateExtent.PARTIAL);
  }
}
