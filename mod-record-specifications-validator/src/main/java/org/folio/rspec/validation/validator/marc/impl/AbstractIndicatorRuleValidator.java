package org.folio.rspec.validation.validator.marc.impl;

import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;

abstract class AbstractIndicatorRuleValidator implements SpecificationRuleValidator<MarcField, SpecificationFieldDto> {

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
                                       SpecificationFieldDto fieldDefinition) {
    var message = translationProvider.format(ruleCode());
    return ValidationError.builder()
      .path(marcIndicator.reference().toString())
      .definitionType(definitionType())
      .definitionId(fieldDefinition.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
