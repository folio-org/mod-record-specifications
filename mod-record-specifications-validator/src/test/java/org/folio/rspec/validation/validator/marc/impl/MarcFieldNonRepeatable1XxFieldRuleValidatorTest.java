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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcFieldNonRepeatable1XxFieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private MarcFieldNonRepeatable1xxFieldRuleValidator validator;

  @Test
  void validate_whenRepeated1xxField_shouldReturnValidationError() {
    var fieldDefinition = new SpecificationFieldDto().id(UUID.randomUUID()).repeatable(false);
    var marcField = new MarcDataField(Reference.forTag("100", 1), null, null);

    when(translationProvider.format(validator.supportedRule().getCode())).thenReturn("message");

    var errors = validator.validate(marcField, fieldDefinition);

    assertEquals(1, errors.size());
    assertEquals(validator.definitionType(), errors.get(0).getDefinitionType());
    assertEquals(validator.severity(), errors.get(0).getSeverity());
    assertEquals(validator.supportedRule().getCode(), errors.get(0).getRuleCode());
    assertEquals("message", errors.get(0).getMessage());
  }

  @ParameterizedTest
  @CsvSource({
    "110, 0",
    "650, 0",
    "650, 1"
  })
  void validate_whenValid1xxField_shouldReturnEmptyList(String tag, int tagIndex) {
    var fieldDefinition = new SpecificationFieldDto().id(UUID.randomUUID()).repeatable(false);
    var marcField = new MarcDataField(Reference.forTag(tag, tagIndex), null, null);

    List<ValidationError> errors = validator.validate(marcField, fieldDefinition);

    assertTrue(errors.isEmpty());
  }
}
