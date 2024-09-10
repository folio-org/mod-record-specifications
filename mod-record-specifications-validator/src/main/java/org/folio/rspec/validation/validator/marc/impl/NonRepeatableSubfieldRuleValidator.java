package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.utils.SpecificationUtils;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;

public class NonRepeatableSubfieldRuleValidator
  implements SpecificationRuleValidator<List<MarcSubfield>, SpecificationFieldDto> {

  private static final String CODE_KEY = "code";

  private final TranslationProvider translationProvider;

  NonRepeatableSubfieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(List<MarcSubfield> marcSubfields, SpecificationFieldDto specification) {
    var nonRepeatableSubfields = SpecificationUtils.nonRepeatableSubfields(specification.getSubfields());

    return marcSubfields.stream()
      .collect(Collectors.groupingBy(MarcSubfield::code))
      .entrySet()
      .stream()
      .filter(subfield -> subfield.getValue().size() > 1 && nonRepeatableSubfields.get(subfield.getKey()) != null)
      .map(subfield -> buildError(subfield.getValue().get(0), nonRepeatableSubfields.get(subfield.getKey())))
      .toList();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.NON_REPEATABLE_SUBFIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.SUBFIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  private ValidationError buildError(MarcSubfield marcSubfield, SubfieldDto subfieldDto) {
    var message = translationProvider.format(ruleCode(), CODE_KEY, marcSubfield.code());
    return ValidationError.builder()
      .path(marcSubfield.reference().toString())
      .definitionType(definitionType())
      .definitionId(subfieldDto != null ? subfieldDto.getId() : null)
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
