package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcControlField;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;
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
class FieldSetMissingFieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private FieldSetMissingFieldRuleValidator validator;

  @Test
  void validate_whenRequiredFieldDoesNotExist_returnValidationError() {
    var tag = "001";
    var fieldDefinition = new SpecificationFieldDto().id(UUID.randomUUID()).tag(tag).required(true);
    var specification = new SpecificationDto().fields(List.of(fieldDefinition));

    Map<String, List<MarcField>> marcFields = new HashMap<>();

    var expectedErrorMessage = "error-message";
    when(translationProvider.format(any(), any(), any())).thenReturn(expectedErrorMessage);

    List<ValidationError> errors = validator.validate(marcFields, specification);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains(expectedErrorMessage));
  }

  @MethodSource("validationDataProvider")
  @ParameterizedTest
  void validate_whenRequiredFieldAndEmptyMarcField_returnValidationError(MarcField marcField) {
    var tag = "001";
    Map<String, List<MarcField>> marcFields = Collections.singletonMap(tag, List.of(marcField));

    var fieldDefinition = new SpecificationFieldDto().id(UUID.randomUUID()).tag(tag).required(true);
    var specification = new SpecificationDto().fields(List.of(fieldDefinition));

    var expectedErrorMessage = "error-message";
    when(translationProvider.format(any(), any(), any())).thenReturn(expectedErrorMessage);

    var errors = validator.validate(marcFields, specification);

    verify(translationProvider).format(validator.supportedRule().getCode(), "tag", tag);

    assertEquals(1, errors.size());
    var error = errors.get(0);
    assertEquals(validator.definitionType(), error.getDefinitionType());
    assertEquals(validator.severity(), error.getSeverity());
    assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
    assertEquals(expectedErrorMessage, error.getMessage());
  }

  static Stream<Arguments> validationDataProvider() {
    var tagRef = Reference.forTag("001");
    return Stream.of(
      Arguments.of(new MarcControlField(tagRef, null)),
      Arguments.of(new MarcControlField(tagRef, "")),
      Arguments.of(new MarcControlField(tagRef, "   ")),
      Arguments.of(new MarcDataField(tagRef, Collections.emptyList(),
        List.of(new MarcSubfield(Reference.forSubfield(tagRef, 'a'), null),
          new MarcSubfield(Reference.forSubfield(tagRef, 'b'), ""),
          new MarcSubfield(Reference.forSubfield(tagRef, 'c'), "  "))))
    );
  }
}
