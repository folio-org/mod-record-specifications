package org.folio.rspec.service.validation.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.builders.FieldBuilder.local;

import org.folio.rspec.exception.ResourceValidationFailedException;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
class FieldValidatorTest {

  private final FieldValidator validator = new FieldValidator();

  @ParameterizedTest
  @ValueSource(strings = {"010", "100", "111", "220", "310", "414", "512", "635", "789", "871", "993"})
  void validateFieldResourceCreate_positive(String fieldTag) {
    var resource = "test";
    var field = local().tag(fieldTag).buildEntity();

    validator.validateFieldResourceCreate(field, resource);
  }

  @ParameterizedTest
  @ValueSource(strings = {"000", "001", "005", "009"})
  void validateFieldResourceCreate_negative(String fieldTag, @Random String resource) {
    var field = local().tag(fieldTag).buildEntity();

    var result = Assertions.assertThrows(ResourceValidationFailedException.class,
      () -> validator.validateFieldResourceCreate(field, resource));

    assertThat(result.getMessage()).contains(resource);
  }
}
