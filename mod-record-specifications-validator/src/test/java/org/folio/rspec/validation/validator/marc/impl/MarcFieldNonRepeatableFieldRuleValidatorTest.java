package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcFieldNonRepeatableFieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private MarcFieldNonRepeatableFieldRuleValidator validator;

  @Test
  void validate_whenRepeatedField_shouldReturnValidationError() {
    var fieldDefinition = new SpecificationFieldDto().id(UUID.randomUUID()).repeatable(false);
    var marcField = new MarcDataField(Reference.forTag("tag", 1), null, null);

    when(translationProvider.format(validator.supportedRule().getCode())).thenReturn("message");

    var errors = validator.validate(marcField, fieldDefinition);

    assertEquals(1, errors.size());
    var validationError = errors.getFirst();
    assertEquals(validator.definitionType(), validationError.getDefinitionType());
    assertEquals(validator.severity(), validationError.getSeverity());
    assertEquals(validator.supportedRule().getCode(), validationError.getRuleCode());
    assertEquals("message", validationError.getMessage());
  }

  @Test
  void validate_whenSingleOccurrence_shouldReturnEmptyList() {
    var fieldDefinition = new SpecificationFieldDto().id(UUID.randomUUID()).repeatable(false);
    var marcField = new MarcDataField(Reference.forTag("tag", 0), null, null);

    List<ValidationError> errors = validator.validate(marcField, fieldDefinition);

    assertTrue(errors.isEmpty());
  }
}
