package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.utils.SpecificationUtils;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

class MarcFieldUndefinedFieldRuleValidator
  implements SpecificationRuleValidator<Map<String, List<MarcField>>, SpecificationDto> {

  private final TranslationProvider translationProvider;

  MarcFieldUndefinedFieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(Map<String, List<MarcField>> fieldMap,
                                        SpecificationDto specification) {
    return fieldMap.entrySet().stream()
      .filter(entry -> SpecificationUtils.findField(specification, entry.getKey()).isEmpty())
      .flatMap(entry -> entry.getValue().stream())
      .map(marcField -> buildError(marcField, specification))
      .toList();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.UNDEFINED_FIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.FIELD;
  }

  private ValidationError buildError(MarcField marcField,
                                     SpecificationDto specificationDto) {
    var message = translationProvider.format(ruleCode());
    return ValidationError.builder()
      .path(marcField.reference().toString())
      .definitionType(definitionType())
      .definitionId(specificationDto.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
