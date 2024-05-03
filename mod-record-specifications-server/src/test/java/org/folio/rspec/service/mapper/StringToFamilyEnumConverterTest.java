package org.folio.rspec.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.rspec.domain.dto.Family;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class StringToFamilyEnumConverterTest {

  private final StringToFamilyEnumConverter converter = new StringToFamilyEnumConverter();

  @Test
  void convert_ReturnsFamilyEnum_WhenSourceIsNotBlank() {
    // Arrange
    String source = "MARC";

    // Act
    Family result = converter.convert(source);

    // Assert
    assertEquals(Family.MARC, result);
  }

  @Test
  void convert_ReturnsNull_WhenSourceIsBlank() {
    // Arrange
    String source = "";

    // Act
    Family result = converter.convert(source);

    // Assert
    assertNull(result);
  }

  @Test
  void convert_ReturnsNull_WhenSourceIsNull() {
    // Act
    Family result = converter.convert(null);

    // Assert
    assertNull(result);
  }

}
