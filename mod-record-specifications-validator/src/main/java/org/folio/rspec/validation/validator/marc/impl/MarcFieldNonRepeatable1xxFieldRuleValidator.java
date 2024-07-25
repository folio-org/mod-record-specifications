package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.utils.TagsMatcher;

class MarcFieldNonRepeatable1xxFieldRuleValidator
  implements SpecificationRuleValidator<MarcField, SpecificationFieldDto> {

  private final TranslationProvider translationProvider;

  MarcFieldNonRepeatable1xxFieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(MarcField marcField, SpecificationFieldDto field) {
    if (TagsMatcher.matches1xx(marcField.tag())
      && Boolean.FALSE.equals(field.getRepeatable()) && marcField.reference().getTagIndex() > 0) {
      return List.of(buildError(marcField, field));
    }
    return List.of();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.NON_REPEATABLE_1XX_FIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.FIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  private ValidationError buildError(MarcField marcField,
                                     SpecificationFieldDto fieldDefinition) {
    var message = translationProvider.format(ruleCode());
    return ValidationError.builder()
      .path(marcField.reference().toString())
      .definitionType(definitionType())
      .definitionId(fieldDefinition.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
