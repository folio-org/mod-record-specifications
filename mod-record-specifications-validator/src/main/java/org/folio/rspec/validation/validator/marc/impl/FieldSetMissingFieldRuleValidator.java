package org.folio.rspec.validation.validator.marc.impl;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.utils.SpecificationUtils;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.rspec.validation.validator.marc.model.Reference;

class FieldSetMissingFieldRuleValidator
  implements SpecificationRuleValidator<Map<String, List<MarcField>>, SpecificationDto> {

  private final TranslationProvider translationProvider;

  FieldSetMissingFieldRuleValidator(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
  }

  @Override
  public List<ValidationError> validate(Map<String, List<MarcField>> fields, SpecificationDto specification) {
    var requiredFields = SpecificationUtils.requiredFields(specification);
    var requiredTags = requiredFields.keySet();
    var recordTags = fields.keySet();
    var existed = Sets.intersection(requiredTags, recordTags);
    var missed = Sets.difference(requiredTags, recordTags);

    List<ValidationError> errors = new ArrayList<>();
    for (String missedTag : missed) {
      var fieldDto = requiredFields.get(missedTag);
      var validationError = prepareError(Reference.forTag(missedTag), fieldDto);
      errors.add(validationError);
    }
    for (String existedTag : existed) {
      for (MarcField marcField : fields.get(existedTag)) {
        if (!marcField.hasValue()) {
          var validationError = prepareError(marcField.reference(), requiredFields.get(existedTag));
          errors.add(validationError);
        }
      }
    }

    return errors;
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return MarcRuleCode.MISSING_FIELD;
  }

  @Override
  public DefinitionType definitionType() {
    return DefinitionType.FIELD;
  }

  @Override
  public SeverityType severity() {
    return SeverityType.ERROR;
  }

  private ValidationError prepareError(Reference missedField, SpecificationFieldDto fieldDefinition) {
    var message = translationProvider.format(ruleCode(), "tag", missedField.getTag());
    return ValidationError.builder()
      .path(missedField.toString())
      .definitionType(definitionType())
      .definitionId(fieldDefinition.getId())
      .severity(SeverityType.ERROR)
      .ruleCode(ruleCode())
      .message(message)
      .build();
  }
}
