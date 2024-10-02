package org.folio.rspec.service.processor.request;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.rspec.service.processor.request.strategy.UpdateRequestProcessingStrategy;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class UpdateRequestEventProcessorTest {

  private @Mock UpdateRequestProcessingStrategy strategyOne;
  private @Mock UpdateRequestProcessingStrategy strategyTwo;

  private UpdateRequestEventProcessor updateRequestProcessor;

  @BeforeEach
  void setUp() {
    when(strategyOne.getType()).thenReturn(DefinitionType.SUBFIELD);
    when(strategyTwo.getType()).thenReturn(DefinitionType.FIELD);

    updateRequestProcessor = new UpdateRequestEventProcessor(List.of(strategyOne, strategyTwo));
  }

  @Test
  void shouldProcessRequestWithCorrectStrategy() {
    var event = getEventFor(DefinitionType.SUBFIELD);

    updateRequestProcessor.process(event);

    verify(strategyOne).process(event);
    verify(strategyTwo, never()).process(event);
  }

  @Test
  void shouldProcessRequestWithSecondStrategy() {
    var event = getEventFor(DefinitionType.FIELD);

    updateRequestProcessor.process(event);

    verify(strategyTwo).process(event);
    verify(strategyOne, never()).process(event);
  }

  @Test
  void shouldThrowExceptionForUndefinedStrategy() {
    var event = getEventFor(DefinitionType.INDICATOR);

    assertThrows(IllegalArgumentException.class, () -> updateRequestProcessor.process(event));

    verify(strategyOne, never()).process(any());
    verify(strategyTwo, never()).process(any());
  }

  private UpdateRequestEvent getEventFor(DefinitionType indicator) {
    return new UpdateRequestEvent() {

      @Override
      public DefinitionType getDefinitionType() {
        return indicator;
      }
    };
  }
}
