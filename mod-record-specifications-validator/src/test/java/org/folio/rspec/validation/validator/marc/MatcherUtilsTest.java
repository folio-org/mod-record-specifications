package org.folio.rspec.validation.validator.marc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.rspec.validation.validator.marc.utils.MatcherUtils;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class MatcherUtilsTest {

  @ParameterizedTest
  @ValueSource(strings = {"000", "123", "555", "876", "999"})
  void whenValidTagValue_shouldValidateTag(String tag) {
    assertTrue(MatcherUtils.matchesValidTag(tag));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0OO", "I23", "5S5", "66", "1234"})
  void whenInvalidTagValue_shouldNotValidateTag(String tag) {
    assertFalse(MatcherUtils.matchesValidTag(tag));
  }

  @ParameterizedTest
  @ValueSource(strings = {"100", "150", "199"})
  void whenValid1xxValue_shouldValidateTag(String tag) {
    assertTrue(MatcherUtils.matches1xxTag(tag));
  }

  @ParameterizedTest
  @ValueSource(strings = {"099", "299", "911"})
  void whenInvalid1xxValue_shouldNotValidateTag(String tag) {
    assertFalse(MatcherUtils.matches1xxTag(tag));
  }

  @ParameterizedTest
  @ValueSource(chars = {'#', '0', '5', '9', 'a', 'o', 'z'})
  void whenValidIndicatorValue_shouldValidateIndicator(char indicator) {
    assertTrue(MatcherUtils.matchesValidIndicator(indicator));
  }

  @ParameterizedTest
  @ValueSource(chars = {'!', ' ', '/', '\\', 'X', 'O', 'I'})
  void whenInvalidIndicatorValue_shouldNotValidateIndicator(char indicator) {
    assertFalse(MatcherUtils.matchesValidIndicator(indicator));
  }
}
