package org.folio.rspec.service.processor.request;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.rspec.service.processor.request.strategy.UpdateRequestProcessingStrategy;
import org.springframework.stereotype.Component;

@Component
public class UpdateRequestEventProcessor {

  private final Map<DefinitionType, UpdateRequestProcessingStrategy> strategyMap = new EnumMap<>(DefinitionType.class);

  public UpdateRequestEventProcessor(List<UpdateRequestProcessingStrategy> strategies) {
    strategies.forEach(strategy -> strategyMap.put(strategy.getType(), strategy));
  }

  public void process(UpdateRequestEvent updateRequestEvent) {
    var type = updateRequestEvent.getDefinitionType();
    UpdateRequestProcessingStrategy strategy = strategyMap.get(type);
    if (strategy != null) {
      strategy.process(updateRequestEvent);
    } else {
      throw new IllegalArgumentException("No processing strategy found for type: " + type);
    }
  }
}
