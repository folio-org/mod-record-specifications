package org.folio.rspec.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.folio.support.TestDataProvider.getSpecification;
import static org.folio.support.TestDataProvider.getSpecificationWithIndicators;
import static org.folio.support.TestDataProvider.getSpecificationWithNonRepeatableSubfields;
import static org.folio.support.TestDataProvider.getSpecificationWithSubfields;
import static org.folio.support.TestDataProvider.getSpecificationWithTags;

import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.TestRecordProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class MarcSpecificationGuidedValidatorTest {

  private final TranslationProvider translationProvider = (key, args) -> "message";
  private final SpecificationGuidedValidator validator = new SpecificationGuidedValidator(translationProvider);

  @Test
  void testMarcRecordValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/marc-bib-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecification());
    assertThat(validationErrors)
      .hasSize(4)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("889[0]", MarcRuleCode.MISSING_FIELD.getCode()),
        tuple("047[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("650[1]", MarcRuleCode.NON_REPEATABLE_FIELD.getCode()),
        tuple("650[2]", MarcRuleCode.NON_REPEATABLE_FIELD.getCode())
      );
  }

  @Test
  void testMarcRecordFieldsValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/marc-invalid-tag-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithTags("100"));
    assertThat(validationErrors)
      .hasSize(6)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("1OO[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("11I[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("XXX[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("1OO[0]", MarcRuleCode.INVALID_FIELD_TAG.getCode()),
        tuple("11I[0]", MarcRuleCode.INVALID_FIELD_TAG.getCode()),
        tuple("XXX[0]", MarcRuleCode.INVALID_FIELD_TAG.getCode())
      );
  }

  @ParameterizedTest
  @MethodSource("provide1xxArguments")
  void test1xxMarcRecordValidation(String file, String[] tags, Tuple[] expected) {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord(String.format("testdata/tag1xx/%s.json", file));

    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithTags(tags));

    assertThat(validationErrors)
      .hasSize(expected.length)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(expected);
  }

  @Test
  void testInvalidIndicatorValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/indicators/marc-indicators-record.json");

    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithIndicators());

    assertThat(validationErrors)
      .hasSize(8)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("035[0]^2", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("035[1]^1", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("035[2]^1", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("035[2]^2", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("047[0]^2", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("047[1]^1", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("047[2]^1", MarcRuleCode.INVALID_INDICATOR.getCode()),
        tuple("047[2]^2", MarcRuleCode.INVALID_INDICATOR.getCode())
      );
  }

  @Test
  void testUndefinedIndicatorValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord(
      "testdata/indicators/marc-undefined-indicators-record.json");

    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithIndicators());

    assertThat(validationErrors)
      .hasSize(14)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("035[0]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("035[1]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("047[0]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("047[0]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("047[1]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("047[2]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("100[0]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("100[0]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("245[0]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("245[1]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("245[2]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("245[2]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("245[3]^1", MarcRuleCode.UNDEFINED_INDICATOR.getCode()),
        tuple("245[3]^2", MarcRuleCode.UNDEFINED_INDICATOR.getCode())
      );
  }

  @Test
  void testMissingSubfieldValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/subfields/marc-subfield-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithSubfields());
    assertThat(validationErrors)
      .hasSize(10)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("650[1]$a[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("650[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("035[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("047[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("245[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("246[0]$a[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("246[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("246[2]$a[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("246[2]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("010[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode())
      );
  }

  @Test
  void testUndefinedSubfieldValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/subfields/marc-undefined-subfields-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithSubfields());
    assertThat(validationErrors)
      .hasSize(8)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("650[0]$w[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode()),
        tuple("035[0]$b[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode()),
        tuple("035[0]$d[0]", MarcRuleCode.MISSING_SUBFIELD.getCode()),
        tuple("035[0]$t[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode()),
        tuple("047[0]$f[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode()),
        tuple("047[0]$r[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode()),
        tuple("245[0]$c[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode()),
        tuple("245[1]$c[0]", MarcRuleCode.UNDEFINED_SUBFIELD.getCode())
      );
  }

  @Test
  void testNonRepeatableSubfieldValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord(
      "testdata/subfields/marc-non-repeatable-subfields-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecificationWithNonRepeatableSubfields());
    assertThat(validationErrors)
      .hasSize(4)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("650[0]$w[1]", MarcRuleCode.NON_REPEATABLE_SUBFIELD.getCode()),
        tuple("047[0]$d[1]", MarcRuleCode.NON_REPEATABLE_SUBFIELD.getCode()),
        tuple("245[0]$d[1]", MarcRuleCode.NON_REPEATABLE_SUBFIELD.getCode()),
        tuple("035[0]$w[1]", MarcRuleCode.NON_REPEATABLE_SUBFIELD.getCode())
      );
  }

  private static Stream<Arguments> provide1xxArguments() {
    return Stream.of(
      // Multiple 1xx fields with same undefined tag
      Arguments.of("marc-same1xx-record", new String[] { }, sameUndefinedTagValidationErrors()),
      // Multiple 1xx fields with same defined tag
      Arguments.of("marc-same1xx-record", new String[] {"100"}, sameDefinedTagValidationErrors()),
      // Multiple 1xx fields with different tags (one undefined tag)
      Arguments.of("marc-different1xx-record", new String[] {"100"}, differentTagsValidationErrors()),
      // Multiple 1xx fields with different defined tags
      Arguments.of("marc-different1xx-record", new String[] {"100", "130"}, differentDefinedTagsValidationErrors()),
      // No 1xx fields
      Arguments.of("marc-no1xx-record", new String[] { }, missing1xxValidationError())
    );
  }

  private static Tuple[] missing1xxValidationError() {
    return new Tuple[] {
      tuple("1XX[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
    };
  }

  private static Tuple[] differentDefinedTagsValidationErrors() {
    return new Tuple[] {
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("130[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
      tuple("130[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
    };
  }

  private static Tuple[] differentTagsValidationErrors() {
    return new Tuple[] {
      tuple("130[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("130[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
      tuple("130[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
    };
  }

  private static Tuple[] sameDefinedTagValidationErrors() {
    return new Tuple[] {
      tuple("100[1]", MarcRuleCode.NON_REPEATABLE_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("100[1]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
      tuple("100[1]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
    };
  }

  private static Tuple[] sameUndefinedTagValidationErrors() {
    return new Tuple[] {
      tuple("100[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
      tuple("100[1]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("100[1]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
      tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
      tuple("100[1]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
    };
  }
}
