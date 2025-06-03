package org.folio.rspec.integration.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaFolioContextExecutorTest {

  @Mock
  private FolioModuleMetadata moduleMetadata;
  @Mock
  private Runnable runnable;

  @InjectMocks
  private KafkaFolioContextExecutor kafkaFolioContextExecutor;

  @Test
  void shouldRunRunnableInContext() {
    // Arrange
    Map<String, Object> headersMap = new HashMap<>();
    headersMap.put(XOkapiHeaders.TENANT, "tenantId".getBytes());
    headersMap.put(XOkapiHeaders.URL, "http://localhost".getBytes());
    headersMap.put(XOkapiHeaders.TOKEN, "token".getBytes());
    headersMap.put(XOkapiHeaders.USER_ID, UUID.randomUUID().toString().getBytes());
    MessageHeaders headers = new MessageHeaders(headersMap);

    // Act
    kafkaFolioContextExecutor.runInContext(headers, runnable);

    // Assert
    verify(runnable).run();
  }

  @Test
  void shouldGetEmptyListWhenHeaderNotPresent() {
    // Arrange
    var tenantId = "tenantId";
    var url = "http://localhost";
    var token = "token";
    var userId = UUID.randomUUID();
    Map<String, Object> headersMap = new HashMap<>();
    headersMap.put(XOkapiHeaders.TENANT, tenantId.getBytes());
    headersMap.put(XOkapiHeaders.URL, url.getBytes());
    headersMap.put(XOkapiHeaders.TOKEN, token.getBytes());
    headersMap.put(XOkapiHeaders.USER_ID, userId.toString().getBytes());
    MessageHeaders headers = new MessageHeaders(headersMap);

    // Act
    var result = KafkaFolioContextExecutor.getContextFromMessageHeaders(headers, moduleMetadata);

    // Assert
    assertThat(result)
      .extracting(FolioExecutionContext::getTenantId,
        FolioExecutionContext::getOkapiUrl,
        FolioExecutionContext::getToken,
        FolioExecutionContext::getUserId)
      .containsExactly(tenantId, url, token, userId);
  }

  @Test
  void shouldHandleAbsentHeader() {
    // Arrange
    MessageHeaders headers = new MessageHeaders(Collections.emptyMap());

    // Act
    var result = KafkaFolioContextExecutor.getContextFromMessageHeaders(headers, moduleMetadata);

    // Assert
    assertThat(result)
      .extracting(FolioExecutionContext::getTenantId,
        FolioExecutionContext::getOkapiUrl,
        FolioExecutionContext::getToken,
        FolioExecutionContext::getUserId)
      .containsExactly("", "", "", null);
  }
}
