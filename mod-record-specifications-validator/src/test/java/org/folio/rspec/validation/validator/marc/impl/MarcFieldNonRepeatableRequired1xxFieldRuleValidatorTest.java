package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcFieldNonRepeatableRequired1xxFieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private MarcFieldNonRepeatableRequired1xxFieldRuleValidator validator;

  @Test
  void validate_whenMoreThanOne1xxField_shouldReturnValidationError() {
    Map<String, List<MarcField>> fields = Map.of(
      "001", Collections.singletonList(new MarcDataField(Reference.forTag("001"), List.of(), List.of())),
      "111", Collections.singletonList(new MarcDataField(Reference.forTag("111"), List.of(), List.of())),
      "123", Collections.singletonList(new MarcDataField(Reference.forTag("123"), List.of(), List.of())),
      "650", Collections.singletonList(new MarcDataField(Reference.forTag("650"), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of());
    when(translationProvider.format(validator.supportedRule().getCode())).thenReturn("1xx error message");

    var errors = validator.validate(fields, specification);

    assertEquals(2, errors.size());
    errors.forEach(error -> {
      assertEquals(validator.definitionType(), error.getDefinitionType());
      assertEquals(validator.severity(), error.getSeverity());
      assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
      assertEquals("1xx error message", error.getMessage());
      assertEquals(MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode(), error.getRuleCode());
    });
  }

  @Test
  void validate_whenNo1xxField_shouldReturnValidationError() {
    Map<String, List<MarcField>> fields = Map.of(
      "001", Collections.singletonList(new MarcDataField(Reference.forTag("001"), List.of(), List.of())),
      "650", Collections.singletonList(new MarcDataField(Reference.forTag("650"), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of());
    when(translationProvider.format(validator.supportedRule().getCode())).thenReturn("1xx error message");

    var errors = validator.validate(fields, specification);

    assertEquals(1, errors.size());
    var validationError = errors.getFirst();
    assertEquals(validator.definitionType(), validationError.getDefinitionType());
    assertEquals(validator.severity(), validationError.getSeverity());
    assertEquals(validator.supportedRule().getCode(), validationError.getRuleCode());
    assertEquals("1xx error message", validationError.getMessage());
    assertEquals(MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode(), validationError.getRuleCode());
    assertNotNull(validationError.getPath());
  }

  @ParameterizedTest
  @ValueSource(strings = {"100", "199"})
  void validate_whenSingle1xxField_shouldReturnEmptyList(String tag) {
    Map<String, List<MarcField>> fields = Map.of(
      tag, Collections.singletonList(new MarcDataField(Reference.forTag(tag), List.of(), List.of())),
      "650", Collections.singletonList(new MarcDataField(Reference.forTag("650"), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of(new SpecificationFieldDto().tag("650")));

    var errors = validator.validate(fields, specification);

    assertTrue(errors.isEmpty());
  }
}
