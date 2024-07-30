package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class MarcFieldNonRepeatable1xxFieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @InjectMocks
  private MarcFieldNonRepeatable1xxFieldRuleValidator validator;

  @Test
  void validate_whenMoreThanOne1xxField_shouldReturnValidationError() {
    Map<String, List<MarcField>> fields = Map.of(
      "100", Collections.singletonList(new MarcDataField(Reference.forTag("100"), List.of(), List.of())),
      "110", Collections.singletonList(new MarcDataField(Reference.forTag("110"), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of(
      new SpecificationFieldDto().tag("100"),
      new SpecificationFieldDto().tag("110")
    ));
    when(translationProvider.format(validator.supportedRule().getCode())).thenReturn("message");

    var errors = validator.validate(fields, specification);

    assertEquals(2, errors.size());
    assertEquals(validator.definitionType(), errors.get(0).getDefinitionType());
    assertEquals(validator.severity(), errors.get(0).getSeverity());
    assertEquals(validator.supportedRule().getCode(), errors.get(0).getRuleCode());
    assertEquals("message", errors.get(0).getMessage());
    assertEquals(MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode(), errors.get(0).getRuleCode());
    assertEquals(MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode(), errors.get(1).getRuleCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"100", "110", "130", "199"})
  void validate_whenSingle1xxField_shouldReturnEmptyList(String tag) {
    Map<String, List<MarcField>> fields = Map.of(
      tag, Collections.singletonList(new MarcDataField(Reference.forTag(tag), List.of(), List.of())),
      "650", Collections.singletonList(new MarcDataField(Reference.forTag("650"), List.of(), List.of()))
    );
    var specification = new SpecificationDto().fields(List.of(
      new SpecificationFieldDto().tag(tag),
      new SpecificationFieldDto().tag("650")
    ));

    var errors = validator.validate(fields, specification);

    assertTrue(errors.isEmpty());
  }
}
