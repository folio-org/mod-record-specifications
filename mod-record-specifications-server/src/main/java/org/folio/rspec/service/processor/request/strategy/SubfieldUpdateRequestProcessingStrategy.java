package org.folio.rspec.service.processor.request.strategy;

import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SubfieldUpdateRequestEvent;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.service.SpecificationFieldService;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SubfieldUpdateRequestProcessingStrategy extends CommonUpdateRequestProcessingStrategy {

  private final SpecificationFieldService fieldService;

  protected SubfieldUpdateRequestProcessingStrategy(SpecificationRepository specificationRepository,
                                                    SpecificationFieldService fieldService) {
    super(specificationRepository);
    this.fieldService = fieldService;
  }

  @Override
  public DefinitionType getType() {
    return DefinitionType.SUBFIELD;
  }

  @Override
  protected void process(UpdateRequestEvent event, Specification specification) {
    var subfieldUpdateRequestEvent = (SubfieldUpdateRequestEvent) event;
    var saved = fieldService.saveSubfield(specification.getId(),
      subfieldUpdateRequestEvent.getTargetFieldTag(),
      subfieldUpdateRequestEvent.getSubfield());
    log.debug("process::subfield saved: {}", saved);
  }
}
