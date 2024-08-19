package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.utils.MatcherUtils;

class InvalidIndicatorRuleValidator extends AbstractIndicatorRuleValidator {

  InvalidIndicatorRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(List<MarcIndicator> indicators, SpecificationFieldDto specification) {
    var indicatorSpecificationMap = specification.getIndicators().stream()
      .collect(Collectors.toMap(FieldIndicatorDto::getOrder, Function.identity()));

    return indicators.stream()
      .filter(indicator -> !MatcherUtils.matchesValidIndicator(indicator.value()))
      .map(indicator -> buildError(indicator, indicatorSpecificationMap.get(indicator.order())))
      .toList();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.INVALID_INDICATOR;
  }
}
