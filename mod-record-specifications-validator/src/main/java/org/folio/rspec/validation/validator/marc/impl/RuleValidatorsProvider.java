package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

public final class RuleValidatorsProvider {

  private RuleValidatorsProvider() {
    throw new IllegalStateException("Utility class");
  }

  public static SpecificationRuleValidator<MarcField,
    SpecificationFieldDto> getFieldValidator(MarcRuleCode ruleCode, TranslationProvider translationProvider) {
    return switch (ruleCode) {
      case NON_REPEATABLE_FIELD -> new MarcFieldNonRepeatableFieldRuleValidator(translationProvider);
      default -> null;
    };
  }

  public static SpecificationRuleValidator<Map<String, List<MarcField>>,
    SpecificationDto> getFieldSetValidator(MarcRuleCode ruleCode, TranslationProvider translationProvider) {
    return switch (ruleCode) {
      case UNDEFINED_FIELD -> new MarcFieldUndefinedFieldRuleValidator(translationProvider);
      case MISSING_FIELD -> new FieldSetMissingFieldRuleValidator(translationProvider);
      case INVALID_FIELD_TAG -> new FieldTagRuleValidator(translationProvider);
      case NON_REPEATABLE_1XX_FIELD -> new MarcFieldNonRepeatable1xxFieldRuleValidator(translationProvider);
      case NON_REPEATABLE_REQUIRED_1XX_FIELD ->
        new MarcFieldNonRepeatableRequired1xxFieldRuleValidator(translationProvider);
      default -> null;
    };
  }
}
