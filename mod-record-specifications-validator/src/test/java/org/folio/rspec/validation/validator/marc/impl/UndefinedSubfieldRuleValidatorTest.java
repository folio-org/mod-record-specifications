package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
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
public class UndefinedSubfieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;
  @InjectMocks
  private UndefinedSubfieldRuleValidator validator;

  @ParameterizedTest
  @MethodSource("undefinedSubfieldTestSource")
  void validate_whenUndefinedSubfield_shouldReturnValidationError(char subfield1, char subfield2) {
    when(translationProvider.format(any(), any(), any())).thenReturn("message");

    var errors = validator.validate(getSubfields(subfield1, subfield2), getFieldDefinition());

    assertEquals(1, errors.size());
    ValidationError error = errors.get(0);
    assertEquals(validator.definitionType(), error.getDefinitionType());
    assertEquals(validator.severity(), error.getSeverity());
    assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
    assertEquals("message", error.getMessage());
  }

  @Test
  void validate_whenUndefinedSubfield_shouldReturnEmptyList() {
    List<ValidationError> errors = validator.validate(getSubfields('a', 'b'), getFieldDefinition());

    assertTrue(errors.isEmpty());
  }

  public static Stream<Arguments> undefinedSubfieldTestSource() {
    return Stream.of(
      arguments('f', 'a'),
      arguments('c', 'b'),
      arguments('t', 'k'));
  }

  private static SpecificationFieldDto getFieldDefinition() {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .repeatable(false)
      .tag("tag")
      .subfields(List.of(
        new SubfieldDto().code("a"),
        new SubfieldDto().code("b"),
        new SubfieldDto().code("k")));
  }

  private static List<MarcSubfield> getSubfields(char subfield1, char subfield2) {
    return List.of(
      new MarcSubfield(Reference.forSubfield(Reference.forTag("tag"), subfield1), "subfield value"),
      new MarcSubfield(Reference.forSubfield(Reference.forTag("tag"), subfield2), "subfield value")
    );
  }
}
