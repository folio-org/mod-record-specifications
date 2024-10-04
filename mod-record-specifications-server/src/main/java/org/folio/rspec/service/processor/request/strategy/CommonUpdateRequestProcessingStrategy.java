package org.folio.rspec.service.processor.request.strategy;

import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.exception.UpdateRequestProcessingException;
import org.folio.spring.data.OffsetRequest;

@Log4j2
public abstract class CommonUpdateRequestProcessingStrategy implements UpdateRequestProcessingStrategy {

  private final SpecificationRepository specificationRepository;

  protected CommonUpdateRequestProcessingStrategy(SpecificationRepository specificationRepository) {
    this.specificationRepository = specificationRepository;
  }

  @Override
  public void process(UpdateRequestEvent event) {
    log.info("process::Processing update request: {}", event);
    var family = event.getFamily();
    var profile = event.getProfile();
    var specifications = specificationRepository.findByFamilyAndProfile(family, profile, OffsetRequest.of(0, 1));
    if (specifications.isEmpty()) {
      throw UpdateRequestProcessingException.specificationNotFound(family, profile);
    }

    process(event, specifications.toList().get(0));
  }

  protected abstract void process(UpdateRequestEvent event, Specification specification);
}
