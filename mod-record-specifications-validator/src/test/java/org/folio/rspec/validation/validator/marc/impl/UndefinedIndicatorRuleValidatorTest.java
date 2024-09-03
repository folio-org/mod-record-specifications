package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class UndefinedIndicatorRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private UndefinedIndicatorRuleValidator validator;

  @ParameterizedTest
  @MethodSource("undefinedIndicatorsTestSource")
  void validate_whenUndefinedIndicators_shouldReturnValidationError(char ind1, char ind2) {
    var fieldDefinition = getFieldDefinition(true);
    var marcField = new MarcDataField(
      Reference.forTag("tag", 1),
      getIndicators(ind1, ind2),
      null);

    when(translationProvider.format(anyString(),
      anyString(), anyString(), anyString(), anyString())).thenReturn("message");

    var errors = validator.validate(marcField.indicators(), fieldDefinition);

    assertEquals(1, errors.size());
    assertEquals(validator.definitionType(), errors.get(0).getDefinitionType());
    assertEquals(validator.severity(), errors.get(0).getSeverity());
    assertEquals(validator.supportedRule().getCode(), errors.get(0).getRuleCode());
    assertEquals("message", errors.get(0).getMessage());
  }

  @Test
  void validate_whenNoIndicators_shouldReturnValidationErrorForBothIndicators() {
    var fieldDefinition = getFieldDefinition(false);
    var marcField = new MarcDataField(
      Reference.forTag("tag", 1),
      getIndicators('#', '#'),
      null);

    when(translationProvider.format(anyString(),
      anyString(), anyString(), anyString(), anyString())).thenReturn("message");

    var errors = validator.validate(marcField.indicators(), fieldDefinition);

    assertEquals(2, errors.size());
  }

  @Test
  void validate_whenValidIndicators_shouldReturnEmptyList() {
    var fieldDefinition = getFieldDefinition(true);
    var marcField = new MarcDataField(
      Reference.forTag("tag", 0),
      getIndicators('1', 'a'),
      null);

    List<ValidationError> errors = validator.validate(marcField.indicators(), fieldDefinition);

    assertTrue(errors.isEmpty());
  }

  public static Stream<Arguments> undefinedIndicatorsTestSource() {
    return Stream.of(
      arguments('0', 's'),
      arguments('s', '0'),
      arguments('#', 'l'),
      arguments('l', '#')
    );
  }

  private static SpecificationFieldDto getFieldDefinition(boolean withIndicators) {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .repeatable(false)
      .indicators(withIndicators
        ? List.of(
          new FieldIndicatorDto().order(1).codes(getIndicatorCodes()),
          new FieldIndicatorDto().order(2).codes(getIndicatorCodes()))
        : null);
  }

  private static List<MarcIndicator> getIndicators(char ind1, char ind2) {
    return List.of(
      new MarcIndicator(Reference.forIndicator(Reference.forTag("ind"), 1), ind1),
      new MarcIndicator(Reference.forIndicator(Reference.forTag("ind"), 2), ind2)
    );
  }

  private static List<IndicatorCodeDto> getIndicatorCodes() {
    return List.of(
      new IndicatorCodeDto().code("#"),
      new IndicatorCodeDto().code("0"),
      new IndicatorCodeDto().code("1"),
      new IndicatorCodeDto().code("a"),
      new IndicatorCodeDto().code("b")
    );
  }
}
