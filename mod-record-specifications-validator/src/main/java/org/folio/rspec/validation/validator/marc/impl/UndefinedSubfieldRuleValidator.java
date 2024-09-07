package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;

public class UndefinedSubfieldRuleValidator
  implements SpecificationRuleValidator<List<MarcSubfield>, SpecificationFieldDto> {

  private static final String CODE_KEY = "code";

  private final TranslationProvider translationProvider;

  UndefinedSubfieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(List<MarcSubfield> marcSubfields, SpecificationFieldDto specification) {
    var subfieldDtoMap = getSubfieldDtoMap(specification.getSubfields());
    return marcSubfields.stream()
      .filter(marcSubfield -> marcSubfield.code() != null && subfieldDtoMap.get(marcSubfield.code()) == null)
      .map(this::buildError)
      .toList();
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.UNDEFINED_SUBFIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.SUBFIELD;
  }

  private Map<Character, SubfieldDto> getSubfieldDtoMap(List<SubfieldDto> subfields) {
    return subfields == null ? Map.of() : subfields
      .stream()
      .collect(Collectors.toMap(subfield -> subfield.getCode().charAt(0), Function.identity()));
  }

  private ValidationError buildError(MarcSubfield marcSubfield) {
    var message = translationProvider.format(ruleCode(), CODE_KEY, marcSubfield.code());
    return ValidationError.builder()
      .path(marcSubfield.reference().toString())
      .definitionType(definitionType())
      .definitionId(null)
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
