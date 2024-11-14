package org.folio.rspec.integration.kafka;

import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.spring.FolioExecutionContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("fullChangeProducer")
public class SpecificationFullChangedEventProducer extends SpecificationPartialChangedEventProducer {

  public SpecificationFullChangedEventProducer(KafkaTemplate<String, SpecificationUpdatedEvent> template,
                                               SpecificationRepository specificationRepository,
                                               FolioExecutionContext context) {
    super(template, specificationRepository, context);
  }

  @Override
  protected SpecificationUpdatedEvent.UpdateExtent updateExtent() {
    return SpecificationUpdatedEvent.UpdateExtent.FULL;
  }
}
