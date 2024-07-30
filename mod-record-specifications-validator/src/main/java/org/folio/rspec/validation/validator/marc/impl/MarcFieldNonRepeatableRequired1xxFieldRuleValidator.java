package org.folio.rspec.validation.validator.marc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.folio.rspec.validation.validator.marc.utils.TagsMatcher;

class MarcFieldNonRepeatableRequired1xxFieldRuleValidator
  extends Abstract1xxFieldRuleValidator {

  MarcFieldNonRepeatableRequired1xxFieldRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(Map<String, List<MarcField>> fields, SpecificationDto specification) {
    var errors = new ArrayList<ValidationError>();
    if (fields.keySet().stream().noneMatch(TagsMatcher::matches1xx)) {
      errors.add(buildError(null, specification));
    }
    return errors;
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.NON_REPEATABLE_REQUIRED_1XX_FIELD;
  }
}
