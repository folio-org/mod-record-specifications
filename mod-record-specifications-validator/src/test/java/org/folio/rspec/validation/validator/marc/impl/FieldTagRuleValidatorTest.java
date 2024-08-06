package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FieldTagRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private FieldTagRuleValidator validator;

  @ParameterizedTest
  @ValueSource(strings = {"100", "111", "000"})
  void validate_whenValidFieldTag_returnEmptyList(String fieldTag) {
    Map<String, List<MarcField>> fields = Map.of(fieldTag,
      Collections.singletonList(new MarcDataField(Reference.forTag(fieldTag), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of());

    var errors = validator.validate(fields, specification);

    assertEquals(0, errors.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"10O", "11l", "22", "4444", "noTag"})
  void validate_whenInvalidFieldTag_returnValidationError(String fieldTag) {
    Map<String, List<MarcField>> fields = Map.of(fieldTag,
      Collections.singletonList(new MarcDataField(Reference.forTag(fieldTag), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of());

    var expectedErrorMessage = "Invalid tag error message";
    when(translationProvider.format(any())).thenReturn(expectedErrorMessage);

    var errors = validator.validate(fields, specification);

    assertEquals(1, errors.size());
    assertEquals(validator.definitionType(), errors.get(0).getDefinitionType());
    assertEquals(validator.severity(), errors.get(0).getSeverity());
    assertEquals(validator.supportedRule().getCode(), errors.get(0).getRuleCode());
    assertEquals(expectedErrorMessage, errors.get(0).getMessage());
    assertEquals(MarcRuleCode.INVALID_FIELD_TAG.getCode(), errors.get(0).getRuleCode());
  }
}
