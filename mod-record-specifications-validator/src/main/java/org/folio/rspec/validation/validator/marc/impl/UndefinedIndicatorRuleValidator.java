package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.utils.MatcherUtils;

class UndefinedIndicatorRuleValidator extends AbstractIndicatorRuleValidator {

  UndefinedIndicatorRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(List<MarcIndicator> indicators, List<FieldIndicatorDto> specification) {
    return indicators.stream()
      .filter(indicator -> indicator.value().equals(specification.get(indicator.order() - 1).getCodes().get(0))) // todo
      .map(indicator -> buildError(indicator, specification.get(indicator.order() - 1)))
      .toList();
  }

  @Override
  public SeverityType severity() {
    return SeverityType.WARN;
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.INVALID_INDICATOR;
  }
}
