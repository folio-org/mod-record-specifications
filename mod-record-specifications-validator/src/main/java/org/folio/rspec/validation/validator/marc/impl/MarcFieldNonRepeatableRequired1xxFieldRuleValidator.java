package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

class MarcFieldNonRepeatableRequired1xxFieldRuleValidator
  extends Abstract1xxFieldRuleValidator {

  MarcFieldNonRepeatableRequired1xxFieldRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(Map<String, List<MarcField>> fields, SpecificationDto specification) {
    List<MarcField> all1xxFields = extract1xxFields(fields);
    return switch (all1xxFields.size()) {
      case 1 -> List.of();
      case 0 -> List.of(buildError(null, specification));
      default -> all1xxFields.stream().map(field -> buildError(field, specification)).toList();
    };
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD;
  }
}
