package org.folio.rspec.validation.validator.marc.impl;

import java.util.List;
import java.util.regex.Pattern;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;
import org.folio.rspec.validation.validator.marc.model.Reference;


public class InvalidLccnSubfieldRuleValidator
  implements SpecificationRuleValidator<List<MarcSubfield>, SpecificationFieldDto> {

  private static final String CODE_KEY = "code";
  private static final String TAG_010 = "010";
  private static final String LCCN_SUBFIELD = "a";
  private static final Pattern LCCN_STRUCTURE_A_PATTERN = Pattern.compile("([' ']{3}|[a-z][|a-z]{2})\\d{8}[' ']");
  private static final Pattern LCCN_STRUCTURE_B_PATTERN = Pattern.compile("([' ']{2}|[a-z][|a-z])\\d{10}");

  private final TranslationProvider translationProvider;

  InvalidLccnSubfieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(List<MarcSubfield> subfields, SpecificationFieldDto specificationFieldDto) {
    if (!TAG_010.equals(specificationFieldDto.getTag())) {
      return List.of();
    }

    var lccn = subfields.stream().filter(subfield -> subfield.code() == 'a').findFirst();

    if (lccn.isEmpty()) {
      return List.of();
    }

    if (LCCN_STRUCTURE_A_PATTERN.matcher(lccn.get().value()).matches()
      || LCCN_STRUCTURE_B_PATTERN.matcher(lccn.get().value()).matches()) {
      return List.of();
    }

    var lccnSpecificationDto = specificationFieldDto.getSubfields().stream()
      .filter(subfieldDto -> LCCN_SUBFIELD.equals(subfieldDto.getCode()))
      .findFirst()
      .orElse(null);

    if (lccnSpecificationDto == null) {
      return List.of();
    }

    return List.of(buildError(TAG_010, lccnSpecificationDto));
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.INVALID_LCCN_SUBFIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.SUBFIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  private ValidationError buildError(String tag, SubfieldDto definition) {
    var message = translationProvider.format(ruleCode(), CODE_KEY, definition.getCode());
    return ValidationError.builder()
      .path(Reference.forSubfield(Reference.forTag(tag), definition.getCode().charAt(0)).toString())
      .definitionType(definitionType())
      .definitionId(definition.getId())
      .severity(severity())
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
