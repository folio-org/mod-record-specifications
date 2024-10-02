package org.folio.rspec.service.processor.request.strategy;

import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.UpdateRequestEvent;

public interface UpdateRequestProcessingStrategy {

  void process(UpdateRequestEvent event);

  DefinitionType getType();
}
