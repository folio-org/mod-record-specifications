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

class UndefinedIndicatorRuleValidator extends AbstractIndicatorRuleValidator {

  private static final String ORDER_KEY = "order";
  private static final String CODE_KEY = "code";

  UndefinedIndicatorRuleValidator(TranslationProvider translationProvider) {
    super(translationProvider);
  }

  @Override
  public List<ValidationError> validate(List<MarcIndicator> indicators, SpecificationFieldDto fieldDefinition) {
    var fieldDefinitionIndicatorsMap = fieldDefinition.getIndicators().stream()
      .collect(Collectors.toMap(FieldIndicatorDto::getOrder, Function.identity()));

    return indicators.stream()
      .filter(indicator -> isUndefined(indicator, fieldDefinitionIndicatorsMap.get(indicator.order())))
      .map(indicator -> buildError(indicator, fieldDefinitionIndicatorsMap.get(indicator.order())))
      .toList();
  }

  @Override
  protected ValidationError buildError(MarcIndicator marcIndicator,
                                       FieldIndicatorDto indicatorDefinition) {
    var orderValue = marcIndicator.order() == 1 ? "First" : "Second";
    var message = translationProvider.format(ruleCode(),
      ORDER_KEY, orderValue,
      CODE_KEY, marcIndicator.value().toString()
    );
    return ValidationError.builder()
      .path(marcIndicator.reference().toString())
      .definitionType(definitionType())
      .definitionId(indicatorDefinition != null ? indicatorDefinition.getId() : null)
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }

  @Override
  public SeverityType severity() {
    return SeverityType.WARN;
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.UNDEFINED_INDICATOR;
  }

  private boolean isUndefined(MarcIndicator indicator, FieldIndicatorDto definition) {
    if (definition != null) {
      return definition.getCodes().stream().noneMatch(code -> code.getCode().equals(indicator.value().toString()));
    }
    return true;
  }
}
