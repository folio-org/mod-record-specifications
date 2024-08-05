package org.folio.rspec.validation.validator.marc.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.utils.TagsMatcher;

class FieldTagRuleValidator
  implements SpecificationRuleValidator<Map<String, List<MarcField>>, SpecificationDto> {

  private final TranslationProvider translationProvider;

  FieldTagRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(Map<String, List<MarcField>> fields, SpecificationDto specification) {
    if (fields.keySet().stream().allMatch(TagsMatcher::matchesValidTag)) {
      return List.of();
    }

    return fields.values().stream()
      .flatMap(Collection::stream)
      .map(field -> prepareError(field, specification))
      .toList();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.INVALID_FIELD_TAG;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.FIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  private ValidationError prepareError(MarcField marcField,
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