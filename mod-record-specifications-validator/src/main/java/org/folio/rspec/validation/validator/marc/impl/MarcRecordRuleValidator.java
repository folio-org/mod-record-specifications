package org.folio.rspec.validation.validator.marc.impl;

import static org.folio.rspec.utils.SpecificationUtils.findField;
import static org.folio.rspec.utils.SpecificationUtils.ruleIsEnabled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.SpecificationRuleCode;
import org.folio.rspec.validation.validator.SpecificationRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;

public class MarcRecordRuleValidator implements SpecificationRuleValidator<MarcRecord, SpecificationDto> {

  private final List<SpecificationRuleValidator<Map<String, List<MarcField>>, SpecificationDto>> fieldSetValidators;
  private final List<SpecificationRuleValidator<MarcField, SpecificationFieldDto>> fieldValidators;
  private final List<SpecificationRuleValidator<List<MarcIndicator>, SpecificationFieldDto>> indicatorValidators;
  private final List<SpecificationRuleValidator<List<MarcSubfield>, SpecificationFieldDto>> subfieldValidators;
  private final SpecificationRuleValidator<MarcDataField, SpecificationFieldDto> missingSubfieldValidator;

  public MarcRecordRuleValidator(TranslationProvider translationProvider) {
    this.fieldSetValidators = List.of(
      new FieldTagRuleValidator(translationProvider),
      new FieldSetMissingFieldRuleValidator(translationProvider),
      new MarcFieldUndefinedFieldRuleValidator(translationProvider),
      new MarcFieldNonRepeatable1xxFieldRuleValidator(translationProvider),
      new MarcFieldNonRepeatableRequired1xxFieldRuleValidator(translationProvider)
    );
    this.fieldValidators = List.of(
      new MarcFieldNonRepeatableFieldRuleValidator(translationProvider)
    );
    this.indicatorValidators = List.of(
      // InvalidIndicatorRuleValidator must be first to avoid duplication errors from UndefinedIndicatorRuleValidator
      new InvalidIndicatorRuleValidator(translationProvider),
      new UndefinedIndicatorRuleValidator(translationProvider)

    );
    this.missingSubfieldValidator = new MissingSubfieldRuleValidator(translationProvider);
    this.subfieldValidators = List.of(
      new UndefinedSubfieldRuleValidator(translationProvider),
      new NonRepeatableSubfieldRuleValidator(translationProvider),
      new InvalidLccnSubfieldRuleValidator(translationProvider));
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
        .ifPresent(fieldDefinition -> validateField(marcField, fieldDefinition, specification, validationErrors));
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

  private void validateField(MarcField marcField, SpecificationFieldDto fieldDefinition, SpecificationDto specification,
                             List<ValidationError> validationErrors) {
    for (var validator : fieldValidators) {
      if (ruleIsEnabled(validator.ruleCode(), specification)) {
        validationErrors.addAll(validator.validate(marcField, fieldDefinition));
      }
    }
    if (marcField instanceof MarcDataField field) {
      validateIndicators(specification, fieldDefinition, field, validationErrors);
      validateSubfields(specification, fieldDefinition, field, validationErrors);
    }
  }

  private void validateIndicators(
    SpecificationDto specification, SpecificationFieldDto fieldDefinition,
    MarcDataField field, List<ValidationError> validationErrors) {

    for (var validator : indicatorValidators) {
      if (ruleIsEnabled(validator.ruleCode(), specification)) {
        List<ValidationError> errors = validator.validate(field.indicators(), fieldDefinition);
        List<String> invalidIndicatorsErrorPaths = validationErrors.stream()
          .filter(e -> DefinitionType.INDICATOR.equals(e.getDefinitionType()))
          .map(ValidationError::getPath)
          .toList();
        validationErrors.addAll(errors.stream()
          .filter(error -> !invalidIndicatorsErrorPaths.contains(error.getPath()))
          .toList());
      }
    }
  }

  private void validateSubfields(
    SpecificationDto specification, SpecificationFieldDto fieldDefinition,
    MarcDataField field, List<ValidationError> validationErrors) {

    if (ruleIsEnabled(missingSubfieldValidator.ruleCode(), specification)) {
      validationErrors.addAll(missingSubfieldValidator.validate(field, fieldDefinition));
    }
    for (var validator : subfieldValidators) {
      if (ruleIsEnabled(validator.ruleCode(), specification)) {
        validationErrors.addAll(validator.validate(field.subfields(), fieldDefinition));
      }
    }
  }
}
