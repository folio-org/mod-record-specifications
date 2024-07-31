package org.folio.rspec.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.folio.support.TestDataProvider.getSpecification;
import static org.folio.support.TestDataProvider.getSpecification1xx;

import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.TestRecordProvider;
import org.junit.jupiter.api.Test;

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
  void testSame1xxUndefinedMarcRecordValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/bib1xx/marc-bib-same1xx-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecification1xx());
    assertThat(validationErrors)
      .hasSize(4)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("100[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("100[1]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
        tuple("100[1]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode())
      );
  }

  @Test
  void testSame1xxMarcRecordValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/bib1xx/marc-bib-same1xx-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecification1xx("100"));
    assertThat(validationErrors)
      .hasSize(2)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
        tuple("100[1]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode())
      );
  }

  @Test
  void testDifferent1xxUndefinedMarcRecordValidation() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/bib1xx/marc-bib-different1xx-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecification1xx("100"));
    assertThat(validationErrors)
      .hasSize(3)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("131[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
        tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
        tuple("131[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode())
      );
  }

  @Test
  void testDifferent1xxMarcRecordValidation1() {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/bib1xx/marc-bib-different1xx-record.json");
    var validationErrors = validator.validate(marc4jRecord, getSpecification1xx("100", "131"));
    assertThat(validationErrors)
      .hasSize(2)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(
        tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
        tuple("131[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode())
      );
  }
}
