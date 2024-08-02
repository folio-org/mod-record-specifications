package org.folio.rspec.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.folio.support.TestDataProvider.getSpecification;
import static org.folio.support.TestDataProvider.getSpecification1xx;

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

  @ParameterizedTest
  @MethodSource("provide1xxArguments")
  void test1xxMarcRecordValidation(String file, String[] tags, Tuple[] expected) {
    var marc4jRecord = TestRecordProvider.getMarc4jRecord(String.format("testdata/tag1xx/%s.json", file));

    var validationErrors = validator.validate(marc4jRecord, getSpecification1xx(tags));

    assertThat(validationErrors)
      .hasSize(expected.length)
      .extracting(ValidationError::getPath, ValidationError::getRuleCode)
      .containsExactlyInAnyOrder(expected);
  }

  private static Stream<Arguments> provide1xxArguments() {
    return Stream.of(
      // Multiple 1xx fields with same undefined tag
      Arguments.of("marc-same1xx-record", new String[] {},
        new Tuple[] {
          tuple("100[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
          tuple("100[1]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("100[1]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
          tuple("100[1]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
        }
      ),
      // Multiple 1xx fields with same defined tag
      Arguments.of("marc-same1xx-record", new String[] {"100"},
        new Tuple[] {
          tuple("100[1]", MarcRuleCode.NON_REPEATABLE_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("100[1]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
          tuple("100[1]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
        }
      ),
      // Multiple 1xx fields with different tags (one undefined tag)
      Arguments.of("marc-different1xx-record", new String[] {"100"},
        new Tuple[] {
          tuple("131[0]", MarcRuleCode.UNDEFINED_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("131[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
          tuple("131[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
        }
      ),
      // Multiple 1xx fields with different defined tags
      Arguments.of("marc-different1xx-record", new String[] {"100", "131"},
        new Tuple[] {
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("131[0]", MarcRuleCode.NON_REPEATABLE_1XX_FIELD.getCode()),
          tuple("100[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode()),
          tuple("131[0]", MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
        }
      ),
      // No 1xx fields
      Arguments.of("marc-no1xx-record", new String[] {},
        new Tuple[] {
          tuple(null, MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD.getCode())
        }
      )
    );
  }
}
