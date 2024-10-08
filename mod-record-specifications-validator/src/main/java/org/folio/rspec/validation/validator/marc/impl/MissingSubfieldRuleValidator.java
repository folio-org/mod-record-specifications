package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.utils.SpecificationUtils;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.springframework.util.CollectionUtils;

public class MissingSubfieldRuleValidator
  implements SpecificationRuleValidator<MarcDataField, SpecificationFieldDto> {

  private static final String CODE_KEY = "code";

  private final TranslationProvider translationProvider;

  MissingSubfieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(MarcDataField marcDataField, SpecificationFieldDto specification) {
    var requiredSubFields = SpecificationUtils.requiredSubfields(specification.getSubfields());

    return requiredSubFields.keySet().stream()
      .filter(subFieldCode -> isMissing(marcDataField.subfields(), subFieldCode))
      .map(subFieldCode -> buildError(marcDataField.reference(), requiredSubFields.get(subFieldCode)))
      .toList();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.MISSING_SUBFIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.SUBFIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  private ValidationError buildError(Reference fieldReference, SubfieldDto definition) {
    var message = translationProvider.format(ruleCode(), CODE_KEY, definition.getCode());
    return ValidationError.builder()
      .path(Reference.forSubfield(fieldReference, definition.getCode().charAt(0)).toString())
      .definitionType(definitionType())
      .definitionId(definition.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }

  private boolean isMissing(List<MarcSubfield> marcSubfields, Character subFieldCode) {
    return CollectionUtils.isEmpty(marcSubfields) || marcSubfields.stream()
      .noneMatch(subfield -> isSubfieldEquals(subfield, subFieldCode) && StringUtils.isNotBlank(subfield.value()));
  }

  private boolean isSubfieldEquals(MarcSubfield subfield, Character subFieldCode) {
    return subfield.reference() != null
      && subfield.reference().getSubfield() != null
      && subfield.reference().getSubfield().equals(subFieldCode);
  }
}
