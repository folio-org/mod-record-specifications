package org.folio.rspec.service.validation.scope.field;

import static org.folio.rspec.domain.entity.Field.DEPRECATED_COLUMN;
import static org.folio.rspec.domain.entity.Field.LABEL_COLUMN;
import static org.folio.rspec.domain.entity.Field.REPEATABLE_COLUMN;
import static org.folio.rspec.domain.entity.Field.REQUIRED_COLUMN;
import static org.folio.rspec.domain.entity.Field.TAG_COLUMN;
import static org.folio.rspec.domain.entity.Field.URL_COLUMN;
import static org.folio.support.builders.FieldBuilder.local;
import static org.folio.support.builders.FieldBuilder.standard;
import static org.folio.support.builders.FieldBuilder.system;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class FieldScopeValidatorTest {

  private final FieldLocalScopeValidator localScopeValidator = new FieldLocalScopeValidator();
  private final FieldStandardScopeValidator standardScopeValidator = new FieldStandardScopeValidator();
  private final FieldSystemScopeValidator systemScopeValidator = new FieldSystemScopeValidator();

  @MethodSource("localScopeTestDataPositive")
  @ParameterizedTest(name = "{index}: change={0}")
  void validateChange_local_shouldAllowAllFieldsChange(String fieldName, Field entity,
                                                       SpecificationFieldChangeDto dto) {
    assertDoesNotThrow(() -> localScopeValidator.validateChange(dto, entity));
  }

  @MethodSource("standardScopeTestDataPositive")
  @ParameterizedTest(name = "{index}: change={0}")
  void validateChange_standard_shouldAllowFieldsChange(String fieldName, Field entity,
                                                       SpecificationFieldChangeDto dto) {
    assertDoesNotThrow(() -> standardScopeValidator.validateChange(dto, entity));
  }

  @MethodSource("standardScopeTestDataNegative")
  @ParameterizedTest(name = "{index}: change={0}")
  void validateChange_standard_shouldNotAllowFieldsChange(String fieldName, Field entity,
                                                          SpecificationFieldChangeDto dto) {
    var exception = assertThrows(ScopeModificationNotAllowedException.class,
      () -> standardScopeValidator.validateChange(dto, entity));
    assertEquals(fieldName, exception.getFieldName());
  }

  @MethodSource("systemScopeTestDataPositive")
  @ParameterizedTest(name = "{index}: change={0}")
  void validateChange_system_shouldAllowFieldsChange(String fieldName, Field entity, SpecificationFieldChangeDto dto) {
    assertDoesNotThrow(() -> systemScopeValidator.validateChange(dto, entity));
  }

  @MethodSource("systemScopeTestDataNegative")
  @ParameterizedTest(name = "{index}: change={0}")
  void validateChange_system_shouldNotAllowFieldsChange(String fieldName, Field entity,
                                                        SpecificationFieldChangeDto dto) {
    var exception = assertThrows(ScopeModificationNotAllowedException.class,
      () -> systemScopeValidator.validateChange(dto, entity));
    assertEquals(fieldName, exception.getFieldName());
  }

  @Test
  void validateChange_shouldThrowExceptionForUnexpectedRecordScope() {
    var changeDto = local().buildChangeDto();
    var field = local().buildEntity();
    assertThrows(IllegalStateException.class, () -> standardScopeValidator.validateChange(changeDto, field));
  }

  private static Stream<Arguments> localScopeTestDataPositive() {
    return Stream.of(
      arguments(TAG_COLUMN, local().buildEntity(), local().tag("999").buildChangeDto()),
      arguments(LABEL_COLUMN, local().buildEntity(), local().label("Changed").buildChangeDto()),
      arguments(URL_COLUMN, local().buildEntity(), local().url("http://www.quam.com").buildChangeDto()),
      arguments(DEPRECATED_COLUMN, local().buildEntity(), local().deprecated(false).buildChangeDto()),
      arguments(REPEATABLE_COLUMN, local().buildEntity(), local().repeatable(false).buildChangeDto()),
      arguments(REQUIRED_COLUMN, local().buildEntity(), local().required(false).buildChangeDto())
    );
  }

  private static Stream<Arguments> standardScopeTestDataPositive() {
    return Stream.of(
      arguments(URL_COLUMN, standard().buildEntity(), standard().url("http://www.quam.com").buildChangeDto()),
      arguments(REQUIRED_COLUMN, standard().buildEntity(), standard().required(false).buildChangeDto())
    );
  }

  private static Stream<Arguments> standardScopeTestDataNegative() {
    return Stream.of(
      arguments(TAG_COLUMN, standard().buildEntity(), standard().tag("999").buildChangeDto()),
      arguments(LABEL_COLUMN, standard().buildEntity(), standard().label("Changed").buildChangeDto()),
      arguments(DEPRECATED_COLUMN, standard().buildEntity(), standard().deprecated(false).buildChangeDto()),
      arguments(REPEATABLE_COLUMN, standard().buildEntity(), standard().repeatable(false).buildChangeDto())
    );
  }

  private static Stream<Arguments> systemScopeTestDataPositive() {
    return Stream.of(
      arguments(URL_COLUMN, system().buildEntity(), system().url("http://www.quam.com").buildChangeDto())
    );
  }

  private static Stream<Arguments> systemScopeTestDataNegative() {
    return Stream.of(
      arguments(TAG_COLUMN, system().buildEntity(), system().tag("999").buildChangeDto()),
      arguments(LABEL_COLUMN, system().buildEntity(), system().label("Changed").buildChangeDto()),
      arguments(DEPRECATED_COLUMN, system().buildEntity(), system().deprecated(false).buildChangeDto()),
      arguments(REPEATABLE_COLUMN, system().buildEntity(), system().repeatable(false).buildChangeDto()),
      arguments(REQUIRED_COLUMN, system().buildEntity(), system().required(false).buildChangeDto())
    );
  }
}
