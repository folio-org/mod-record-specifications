package org.folio.rspec.integration.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.tools.kafka.KafkaUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class SpecificationPartialChangedEventProducerTest {

  @InjectMocks
  private SpecificationPartialChangedEventProducer producer;

  @Mock
  private KafkaTemplate<String, SpecificationUpdatedEvent> template;
  @Mock
  private FolioExecutionContext context;

  @Test
  void testSendMessage() {
    var specificationId = UUID.randomUUID();
    var headers = Map.<String, Collection<String>>of("test", List.of("testValue"));

    when(context.getOkapiHeaders()).thenReturn(headers);
    when(context.getTenantId()).thenReturn(TENANT_ID);
    var captor = ArgumentCaptor.<ProducerRecord<String, SpecificationUpdatedEvent>>captor();

    producer.sendEvent(specificationId);

    verify(template).send(captor.capture());
    var captured = captor.getValue();
    assertThat(captured.topic()).matches(topic ->
      topic.endsWith(producer.topicName()) && topic.contains(TENANT_ID));
    assertThat(captured.headers().toArray()).isEqualTo(KafkaUtils.toKafkaHeaders(headers).toArray());
    assertThat(captured.value()).matches(event ->
      TENANT_ID.equals(event.tenantId()) && specificationId.equals(event.specificationId()));
  }
}
