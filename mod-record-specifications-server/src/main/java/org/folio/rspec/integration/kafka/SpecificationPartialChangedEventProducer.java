package org.folio.rspec.integration.kafka;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.FolioExecutionContext;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Primary
@Component
public class SpecificationPartialChangedEventProducer extends EventProducer<UUID, SpecificationUpdatedEvent> {

  private final SpecificationRepository specificationRepository;

  public SpecificationPartialChangedEventProducer(KafkaTemplate<String, SpecificationUpdatedEvent> template,
                                                  SpecificationRepository specificationRepository,
                                                  FolioExecutionContext context) {
    super(template, context);
    this.specificationRepository = specificationRepository;
  }

  @Override
  protected String topicName() {
    return KafkaTopicName.SPECIFICATION_UPDATED.getTopicName();
  }

  @Override
  protected SpecificationUpdatedEvent buildEvent(UUID specificationId) {
    var specification = specificationRepository.findById(specificationId)
      .orElseThrow(() -> ResourceNotFoundException.forSpecification(specificationId));
    return new SpecificationUpdatedEvent(
      specification.getId(),
      tenantId(),
      specification.getFamily(),
      specification.getProfile()
    );
  }

  protected SpecificationUpdatedEvent.UpdateExtent updateExtent() {
    return SpecificationUpdatedEvent.UpdateExtent.PARTIAL;
  }
}
