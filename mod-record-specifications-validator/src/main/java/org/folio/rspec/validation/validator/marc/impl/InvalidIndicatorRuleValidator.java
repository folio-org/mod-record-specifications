package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.utils.MatcherUtils;

class InvalidIndicatorRuleValidator extends AbstractIndicatorRuleValidator {

  InvalidIndicatorRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(MarcField marcField, SpecificationFieldDto field) {
    if (marcField instanceof MarcDataField marcDataField) {
      var indicators = marcDataField.indicators();
      if (indicators != null) {
        return indicators.stream()
          .filter(marcIndicator -> !MatcherUtils.matchesValidIndicator(marcIndicator.value()))
          .map(marcIndicator -> buildError(marcIndicator, field))
          .toList();
      }
    }
    return List.of();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.INVALID_INDICATOR;
  }
}
