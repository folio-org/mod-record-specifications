package org.folio.rspec.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
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
    var record = TestRecordProvider.getMarc4jRecord();
    var validationErrors = validator.validate(record, getSpecification());
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

  private SpecificationDto getSpecification() {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(fieldDefinitions());
  }

  private List<SpecificationFieldDto> fieldDefinitions() {
    return List.of(
      requiredNonRepeatableField("000"),
      requiredNonRepeatableField("001"),
      defaultField("005"),
      defaultField("006"),
      defaultField("007"),
      defaultField("008"),
      defaultField("010"),
      defaultField("035"),
      defaultField("100"),
      requiredField("245"),
      requiredField("889"),
      requiredNonRepeatableField("650")
    );
  }

  private SpecificationFieldDto requiredField(String tag) {
    return fieldDefinition(tag, true, false, true);
  }

  private SpecificationFieldDto requiredNonRepeatableField(String tag) {
    return fieldDefinition(tag, true, false, false);
  }

  private SpecificationFieldDto defaultField(String tag) {
    return fieldDefinition(tag, false, false, true);
  }

  private SpecificationFieldDto fieldDefinition(String tag, boolean required, boolean deprecated, boolean repeatable) {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .tag(tag)
      .required(required)
      .deprecated(deprecated)
      .repeatable(repeatable);
  }

  private List<SpecificationRuleDto> allEnabledRules() {
    return Arrays.stream(MarcRuleCode.values()).map(this::enabledRule).toList();
  }

  private SpecificationRuleDto enabledRule(MarcRuleCode ruleCode) {
    return new SpecificationRuleDto().id(UUID.randomUUID()).code(ruleCode.getCode()).enabled(true);
  }

}
