package org.folio.rspec.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class MetadataMapperTest {

  private final MetadataMapper metadataMapper = source -> null;

  @Test
  void map_returnsFormattedDateTimeString_whenTimestampIsNotNull() {
    // Arrange
    Timestamp timestamp = Timestamp.valueOf("2022-02-14 12:34:56");
    String expectedFormattedDateTime = "2022-02-14 12:34:56";

    // Act
    String result = metadataMapper.map(timestamp);

    // Assert
    assertEquals(expectedFormattedDateTime, result);
  }

  @Test
  void map_returnsNull_whenTimestampIsNull() {
    // Act
    String result = metadataMapper.map(null);

    // Assert
    assertNull(result);
  }
}
