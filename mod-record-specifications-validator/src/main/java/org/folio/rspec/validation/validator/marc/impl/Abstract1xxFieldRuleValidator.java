package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.utils.TagsMatcher;

abstract class Abstract1xxFieldRuleValidator
  implements SpecificationRuleValidator<Map<String, List<MarcField>>, SpecificationDto> {

  private final TranslationProvider translationProvider;

  Abstract1xxFieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.FIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  protected List<MarcField> extract1xxFields(Map<String, List<MarcField>> fields) {
    return fields.keySet().stream()
      .filter(TagsMatcher::matches1xx)
      .flatMap(tag -> fields.get(tag).stream())
      .toList();
  }

  protected ValidationError buildError(MarcField marcField,
                                     SpecificationDto specificationDto) {
    var message = translationProvider.format(ruleCode());
    return ValidationError.builder()
      .path(marcField != null ? marcField.reference().toString() : null)
      .definitionType(definitionType())
      .definitionId(specificationDto.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
