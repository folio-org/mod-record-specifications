package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.springframework.util.CollectionUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class InvalidLccnSubfieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;
  @InjectMocks
  private InvalidLccnSubfieldRuleValidator validator;

  @Test
  void validate_whenNon010Field_shouldReturnEmptyList() {
    var errors = validator.validate(List.of(), new SpecificationFieldDto().tag("tag"));

    assertTrue(CollectionUtils.isEmpty(errors));
  }

  @Test
  void validate_whenLccnSubfieldNotExist_shouldReturnEmptyList() {
    var subfield = new MarcSubfield(Reference.forSubfield(Reference.forTag("010"), 'z'), "12345");

    var errors = validator.validate(List.of(subfield), new SpecificationFieldDto().tag("010"));

    assertTrue(CollectionUtils.isEmpty(errors));
  }

  @ParameterizedTest
  @MethodSource("validLccn")
  void validate_whenValidlccn_shouldReturnEmptyList(String lccn) {
    var subfield = new MarcSubfield(Reference.forSubfield(Reference.forTag("010"), 'a'), lccn);

    var errors = validator.validate(List.of(subfield), new SpecificationFieldDto().tag("010"));

    assertTrue(CollectionUtils.isEmpty(errors));
  }

  @ParameterizedTest
  @MethodSource("invalidLccn")
  void validate_whenInvalidlccn_shouldReturnValidationError(String lccn) {
    when(translationProvider.format(anyString(), anyString(), anyString())).thenReturn("message");
    var subfield = new MarcSubfield(Reference.forSubfield(Reference.forTag("010"), 'a'), lccn);
    var specification = new SpecificationFieldDto().tag("010")
      .subfields(List.of(new SubfieldDto().code("a")));

    var errors = validator.validate(List.of(subfield), specification);

    assertEquals(1, errors.size());
    ValidationError error = errors.get(0);
    assertEquals(validator.definitionType(), error.getDefinitionType());
    assertEquals(validator.severity(), error.getSeverity());
    assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
    assertEquals("message", error.getMessage());
  }

  public static Stream<Arguments> validLccn() {
    return Stream.of(
      // structure A
      arguments("12345678"),
      arguments("n12345678"),
      arguments("nn12345678"),
      arguments("nnn12345678"),

      // structure B
      arguments("0123456789"),
      arguments("n0123456781"),
      arguments("nn0123456789"),
      arguments("nnn0123456789"));
  }

  public static Stream<Arguments> invalidLccn() {
    return Stream.of(
      // invalid structure A
      arguments(" 12345678"),
      arguments("1234567"),
      arguments("123456789"),
      arguments("nnnn12345678"),

      // structure B
      arguments(" 0123456789"),
      arguments("123456789"),
      arguments("nnnn0123456789"));
  }
}
