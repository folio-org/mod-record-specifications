package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
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
class MissingSubfieldRuleValidatorTest {

  private static final String TAG = "245";

  @Mock
  private TranslationProvider translationProvider;
  @InjectMocks
  private MissingSubfieldRuleValidator validator;

  public static Stream<Arguments> missingSubfieldTestSource() {
    return Stream.of(
      arguments('k', 'a', 0),
      arguments('a', 'c', 1),
      arguments('k', 'b', 2));
  }

  @ParameterizedTest
  @MethodSource("missingSubfieldTestSource")
  void validate_whenMissingSubfield_shouldReturnValidationError(char subfield1, char subfield2, int tagIndex) {
    when(translationProvider.format(anyString(), anyString(), anyString())).thenReturn("message");
    var marcDataField = new MarcDataField(
      Reference.forTag(TAG, tagIndex), List.of(), getSubfields(subfield1, subfield2));
    var errors = validator.validate(marcDataField, getFieldDefinition());

    assertEquals(1, errors.size());
    ValidationError error = errors.getFirst();
    assertTrue(error.getPath().startsWith(String.format("%s[%d]", TAG, tagIndex)));
    assertEquals(validator.definitionType(), error.getDefinitionType());
    assertEquals(validator.severity(), error.getSeverity());
    assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
    assertEquals("message", error.getMessage());
  }

  @Test
  void validate_whenMissingSubfield_shouldReturnEmptyList() {
    var marcDataField = new MarcDataField(Reference.forTag(TAG), List.of(), getSubfields('a', 'b'));
    List<ValidationError> errors = validator.validate(marcDataField, getFieldDefinition());

    assertTrue(errors.isEmpty());
  }

  private static SpecificationFieldDto getFieldDefinition() {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .repeatable(false)
      .tag(TAG)
      .subfields(List.of(
        new SubfieldDto().code("a").required(true),
        new SubfieldDto().code("b").required(true),
        new SubfieldDto().code("k").required(false)));
  }

  private static List<MarcSubfield> getSubfields(char subfield1, char subfield2) {
    return List.of(
      new MarcSubfield(Reference.forSubfield(Reference.forTag(TAG), subfield1), "subfield value"),
      new MarcSubfield(Reference.forSubfield(Reference.forTag(TAG), subfield2), "subfield value")
    );
  }
}
