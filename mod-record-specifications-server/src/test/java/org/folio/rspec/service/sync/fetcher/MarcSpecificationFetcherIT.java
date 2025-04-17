package org.folio.rspec.service.sync.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@UnitTest
@Import(JacksonAutoConfiguration.class)
@SpringBootTest(classes = {
  MarcSpecificationFetcher.class
})
class MarcSpecificationFetcherIT {

  @Autowired
  private MarcSpecificationFetcher marcSpecificationFetcher;
  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private MarcSpecificationParser parser;

  @Test
  void testFetch_positive() {
    var expected = objectMapper.createArrayNode();
    when(parser.parse(any())).thenReturn(expected);

    var actual = marcSpecificationFetcher.fetch(Family.MARC, FamilyProfile.AUTHORITY);

    assertEquals(expected, actual);
    verify(parser).parse(any());
  }

  @Test
  void testFetch_negative() {
    when(parser.parse(any())).thenThrow(new RuntimeException("Test exception"));

    assertThrows(SpecificationFetchingFailedException.class,
      () -> marcSpecificationFetcher.fetch(Family.MARC, FamilyProfile.AUTHORITY));
  }

}
