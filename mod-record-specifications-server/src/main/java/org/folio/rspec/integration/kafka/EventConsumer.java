package org.folio.rspec.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.rspec.service.processor.request.UpdateRequestEventProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class EventConsumer {

  private final KafkaFolioContextExecutor executor;
  private final UpdateRequestEventProcessor updateRequestProcessor;

  @KafkaListener(
    containerFactory = "updateRequestEventListenerFactory",
    topicPattern = "#{folioKafkaProperties.listener['update-requests'].topicPattern}",
    groupId = "#{folioKafkaProperties.listener['update-requests'].groupId}",
    concurrency = "#{folioKafkaProperties.listener['update-requests'].concurrency}")
  public void handleSpecUpdateEvents(UpdateRequestEvent updateRequestEvent, MessageHeaders headers) {
    log.info("Received update request [event={}]", updateRequestEvent);
    executor.runInContext(headers, () -> updateRequestProcessor.process(updateRequestEvent));
  }
}
