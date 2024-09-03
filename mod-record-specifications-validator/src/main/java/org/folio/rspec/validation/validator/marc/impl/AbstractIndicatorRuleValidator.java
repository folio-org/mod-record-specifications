package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;

abstract class AbstractIndicatorRuleValidator
  implements SpecificationRuleValidator<List<MarcIndicator>, SpecificationFieldDto> {

  protected final TranslationProvider translationProvider;

  AbstractIndicatorRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.INDICATOR;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  protected Map<Integer, FieldIndicatorDto> getFieldDefinitionIndicatorsMap(List<FieldIndicatorDto> indicators) {
    return indicators == null
      ? Map.of()
      : indicators.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(FieldIndicatorDto::getOrder, Function.identity()));
  }

  protected ValidationError buildError(MarcIndicator marcIndicator,
                                       FieldIndicatorDto indicatorDefinition) {
    var message = translationProvider.format(ruleCode());
    return ValidationError.builder()
      .path(marcIndicator.reference().toString())
      .definitionType(definitionType())
      .definitionId(indicatorDefinition != null ? indicatorDefinition.getId() : null)
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
