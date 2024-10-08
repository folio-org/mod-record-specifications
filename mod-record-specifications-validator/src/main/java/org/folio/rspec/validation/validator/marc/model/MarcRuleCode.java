package org.folio.rspec.validation.validator.marc.model;

import org.folio.rspec.validation.validator.SpecificationRuleCode;

public enum MarcRuleCode implements SpecificationRuleCode {

  UNDEFINED_FIELD("undefinedField"),
  MISSING_FIELD("missingField"),
  INVALID_FIELD_TAG("invalidFieldTag"),
  NON_REPEATABLE_1XX_FIELD("nonRepeatable1XXField"),
  NON_REPEATABLE_REQUIRED_1XX_FIELD("nonRepeatableRequired1XXField"),
  NON_REPEATABLE_FIELD("nonRepeatableField"),
  UNDEFINED_INDICATOR("undefinedIndicatorCode"),
  INVALID_INDICATOR("invalidIndicator"),
  MISSING_SUBFIELD("missingSubfield"),
  UNDEFINED_SUBFIELD("undefinedSubfield"),
  NON_REPEATABLE_SUBFIELD("nonRepeatableSubfield"),
  INVALID_LCCN_SUBFIELD("invalidLccnSubfieldValue");

  private final String code;

  MarcRuleCode(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }
}
