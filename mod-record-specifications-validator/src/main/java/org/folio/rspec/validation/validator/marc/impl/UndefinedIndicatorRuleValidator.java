package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
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
  public List<ValidationError> validate(List<MarcIndicator> indicators, SpecificationFieldDto fieldDefinition) {
    if (!indicators.isEmpty()) {
      return List.of();
    }
    var fieldDefinitionIndicatorsMap = fieldDefinition.getIndicators().stream()
      .collect(Collectors.toMap(FieldIndicatorDto::getOrder, Function.identity()));

    return indicators.stream()
      .filter(indicator -> !MatcherUtils.matchesValidIndicator(indicator.value()))
      .map(indicator -> buildError(indicator, fieldDefinitionIndicatorsMap.get(indicator.order())))
      .toList();
  }

  @Override
  public SeverityType severity() {
    return SeverityType.WARN;
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.UNDEFINED_INDICATOR;
  }
}
