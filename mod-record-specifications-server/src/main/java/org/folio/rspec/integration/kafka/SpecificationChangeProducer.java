package org.folio.rspec.integration.kafka;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.FolioExecutionContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SpecificationChangeProducer extends EventProducer<UUID, SpecificationUpdatedEvent> {

  public static final String SPECIFICATION_UPDATED_TOPIC = "specification-storage.specification.updated";

  public SpecificationChangeProducer(
    KafkaTemplate<String, SpecificationUpdatedEvent> template,
    FolioExecutionContext context) {
    super(template, context);
  }

  @Override
  protected String topicName() {
    return SPECIFICATION_UPDATED_TOPIC;
  }

  @Override
  protected SpecificationUpdatedEvent buildEvent(UUID specificationId) {
    return new SpecificationUpdatedEvent()
      .specificationId(specificationId)
      .tenantId(tenantId());
  }
}
