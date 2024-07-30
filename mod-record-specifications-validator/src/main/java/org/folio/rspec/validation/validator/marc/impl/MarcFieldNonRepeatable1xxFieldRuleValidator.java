package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.utils.TagsMatcher;

class MarcFieldNonRepeatable1xxFieldRuleValidator
  extends Abstract1xxFieldRuleValidator {

  MarcFieldNonRepeatable1xxFieldRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(Map<String, List<MarcField>> fields, SpecificationDto specification) {
    List<String> tags1xx = fields.keySet().stream().filter(TagsMatcher::matches1xx).toList();
    if (tags1xx.size() > 1) {
      return tags1xx.stream()
        .flatMap(tag -> fields.get(tag).stream())
        .map(field -> buildError(field.reference().toString(), specification))
        .toList();
    }
    return List.of();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.NON_REPEATABLE_1XX_FIELD;
  }
}
