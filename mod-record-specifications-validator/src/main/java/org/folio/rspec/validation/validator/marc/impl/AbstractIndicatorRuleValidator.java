package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;

abstract class AbstractIndicatorRuleValidator
  implements SpecificationRuleValidator<List<MarcIndicator>, List<FieldIndicatorDto>> {

  private final TranslationProvider translationProvider;

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

  protected ValidationError buildError(MarcIndicator marcIndicator,
                                       FieldIndicatorDto indicatorDefinition) {
    var message = translationProvider.format(ruleCode());
    return ValidationError.builder()
      .path(marcIndicator.reference().toString())
      .definitionType(definitionType())
      .definitionId(indicatorDefinition.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
