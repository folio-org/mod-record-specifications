package org.folio.rspec.validation.validator.marc.impl;

import static org.folio.rspec.utils.SpecificationUtils.findField;
import static org.folio.rspec.utils.SpecificationUtils.ruleIsEnabled;
import static org.folio.rspec.validation.validator.marc.model.MarcRuleCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

public class MarcRecordRuleValidator implements SpecificationRuleValidator<MarcRecord, SpecificationDto> {

  private final List<SpecificationRuleValidator<Map<String, List<MarcField>>, SpecificationDto>> fieldSetValidators;
  private final List<SpecificationRuleValidator<MarcField, SpecificationFieldDto>> fieldValidators;

  private static final List<MarcRuleCode> FIELD_RULES = List.of(
    NON_REPEATABLE_FIELD
  );
  private static final List<MarcRuleCode> FIELD_SET_RULES = List.of(
    UNDEFINED_FIELD,
    MISSING_FIELD,
    INVALID_FIELD_TAG,
    NON_REPEATABLE_1XX_FIELD,
    NON_REPEATABLE_REQUIRED_1XX_FIELD
  );

  public MarcRecordRuleValidator(TranslationProvider translationProvider) {
    this.fieldSetValidators = FIELD_SET_RULES.stream()
      .map(ruleCode -> RuleValidatorsProvider.getFieldSetValidator(ruleCode, translationProvider))
      .filter(Objects::nonNull)
      .toList();
    this.fieldValidators = FIELD_RULES.stream()
      .map(ruleCode -> RuleValidatorsProvider.getFieldValidator(ruleCode, translationProvider))
      .filter(Objects::nonNull)
      .toList();
  }

  @Override
  public List<ValidationError> validate(MarcRecord marcRecord, SpecificationDto specification) {
    List<ValidationError> validationErrors = new ArrayList<>();
    for (var fieldSetValidator : fieldSetValidators) {
      if (ruleIsEnabled(fieldSetValidator.ruleCode(), specification)) {
        validationErrors.addAll(fieldSetValidator.validate(marcRecord.getAllFields(), specification));
      }
    }
    var marcFields = marcRecord.getAllFields().values().stream()
      .flatMap(List::stream)
      .toList();
    for (MarcField marcField : marcFields) {
      findField(specification, marcField.tag())
        .ifPresent(fieldDefinition -> {
          for (var validator : fieldValidators) {
            if (ruleIsEnabled(validator.ruleCode(), specification)) {
              validationErrors.addAll(validator.validate(marcField, fieldDefinition));
            }
          }
        });

    }
    return validationErrors;
  }

  @Override
  public SpecificationRuleCode supportedRule() {
    return null;
  }

  @Override
  public DefinitionType definitionType() {
    return null;
  }
}
