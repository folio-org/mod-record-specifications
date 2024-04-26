package org.folio.rspec.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class StringToFamilyProfileEnumConverterTest {

  private final StringToFamilyProfileEnumConverter converter = new StringToFamilyProfileEnumConverter();

  @Test
  void convert_ReturnsFamilyEnum_WhenSourceIsNotBlank() {
    // Arrange
    String source = "authority";

    // Act
    FamilyProfile result = converter.convert(source);

    // Assert
    assertEquals(FamilyProfile.AUTHORITY, result);
  }

  @Test
  void convert_ReturnsNull_WhenSourceIsBlank() {
    // Arrange
    String source = "";

    // Act
    FamilyProfile result = converter.convert(source);

    // Assert
    assertNull(result);
  }

  @Test
  void convert_ReturnsNull_WhenSourceIsNull() {
    // Act
    FamilyProfile result = converter.convert(null);

    // Assert
    assertNull(result);
  }
}
