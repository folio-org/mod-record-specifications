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
public class NonRepeatableSubfieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;
  @InjectMocks
  private NonRepeatableSubfieldRuleValidator validator;


  @ParameterizedTest
  @MethodSource("nonRepeatableSubfieldTestSource")
  void validate_whenNonRepeatableSubfield_shouldReturnValidationError(char subfield1, char subfield2, char subfield3) {
    when(translationProvider.format(any(), any(), any())).thenReturn("message");

    var errors = validator.validate(
      List.of(getSubfield(subfield1), getSubfield(subfield2), getSubfield(subfield3)), getFieldDefinition());

    assertEquals(1, errors.size());
    ValidationError error = errors.get(0);
    assertEquals(validator.definitionType(), error.getDefinitionType());
    assertEquals(validator.severity(), error.getSeverity());
    assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
    assertEquals("message", error.getMessage());
  }

  @Test
  void validate_whenNonRepeatableSubfield_shouldReturnEmptyList() {
    List<ValidationError> errors = validator.validate(
      List.of(getSubfield('f'), getSubfield('b'), getSubfield('t')), getFieldDefinition());

    assertTrue(errors.isEmpty());
  }

  public static Stream<Arguments> nonRepeatableSubfieldTestSource() {
    return Stream.of(
      arguments('f', 'a', 'f'),
      arguments('c', 'b', 'b'),
      arguments('t', 't', 'k'));
  }

  private static SpecificationFieldDto getFieldDefinition() {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .repeatable(false)
      .tag("tag")
      .subfields(List.of(
        new SubfieldDto().code("a").repeatable(true),
        new SubfieldDto().code("c").repeatable(true),
        new SubfieldDto().code("k").repeatable(true),
        new SubfieldDto().code("b").repeatable(false),
        new SubfieldDto().code("f").repeatable(false),
        new SubfieldDto().code("t").repeatable(false)));
  }

  private static MarcSubfield getSubfield(char subfield) {
    return new MarcSubfield(Reference.forSubfield(Reference.forTag("tag"), subfield), "subfield value");
  }
}
