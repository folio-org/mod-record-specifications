package org.folio.rspec.integration.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.folio.rspec.domain.dto.SubfieldUpdateRequestEvent;
import org.folio.rspec.service.processor.request.UpdateRequestEventProcessor;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

  private @Mock KafkaFolioContextExecutor executor;
  private @Mock UpdateRequestEventProcessor updateRequestProcessor;

  private @InjectMocks EventConsumer eventConsumer;

  @Test
  void handleSpecUpdateEvents_positive() {
    var headers = new MessageHeaders(Map.of());
    var event = new SubfieldUpdateRequestEvent();
    doAnswer(invocation -> {
      ((Runnable) invocation.getArgument(1)).run();
      return null;
    }).when(executor).runInContext(eq(headers), any());

    eventConsumer.handleSpecUpdateEvents(event, headers);

    verify(updateRequestProcessor).process(event);
  }
}
