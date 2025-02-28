package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.spring.testing.extension.EnableOkapi;
import org.folio.spring.testing.extension.impl.OkapiConfiguration;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@EnableOkapi
@IntegrationTest
class MarcSpecificationFetcherIT extends IntegrationTestBase {

  protected static OkapiConfiguration okapi;

  @Autowired
  private MarcSpecificationFetcher marcSpecificationFetcher;

  @MockBean
  private MarcSpecificationParser parser;

  @Test
  void testRetryLogic() {
    var expected = objectMapper.createArrayNode();

    // Simulate a failure in the parser
    when(parser.parse(any()))
      .thenThrow(new IllegalArgumentException("Simulated failure 1"))
      .thenThrow(new IllegalArgumentException("Simulated failure 2"))
      .thenReturn(expected);

    var actual = marcSpecificationFetcher.fetch(okapi.getOkapiUrl() + "/marc/bibliographic.html");

    assertEquals(expected, actual);

    // Verify that the parser was called multiple times
    verify(parser, times(3)).parse(any());
  }

}
