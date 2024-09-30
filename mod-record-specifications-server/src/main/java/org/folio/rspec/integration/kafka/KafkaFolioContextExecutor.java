package org.folio.rspec.integration.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaFolioContextExecutor {

  private final FolioModuleMetadata moduleMetadata;

  public void runInContext(MessageHeaders headers, Runnable runnable) {
    try (var fec = new FolioExecutionContextSetter(getContextFromMessageHeaders(headers, moduleMetadata))) {
      runnable.run();
    }
  }

  public static FolioExecutionContext getContextFromMessageHeaders(MessageHeaders headers,
                                                                    FolioModuleMetadata moduleMetadata) {
    Map<String, Collection<String>> map = new HashMap<>();
    map.put(XOkapiHeaders.TENANT, getHeaderValue(headers, XOkapiHeaders.TENANT));
    map.put(XOkapiHeaders.URL, getHeaderValue(headers, XOkapiHeaders.URL));
    map.put(XOkapiHeaders.TOKEN, getHeaderValue(headers, XOkapiHeaders.TOKEN));
    map.put(XOkapiHeaders.USER_ID, getHeaderValue(headers, XOkapiHeaders.USER_ID));
    return new DefaultFolioExecutionContext(moduleMetadata, map);
  }

  private static List<String> getHeaderValue(MessageHeaders headers, String headerName) {
    var headerValue = headers.get(headerName);
    return headerValue == null
           ? Collections.emptyList()
           : Collections.singletonList(new String((byte[]) headerValue, StandardCharsets.UTF_8));
  }

}
