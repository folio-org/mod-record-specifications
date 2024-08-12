package org.folio.rspec.validation.validator.marc.model;

import org.folio.rspec.validation.validator.SpecificationRuleCode;

public enum MarcRuleCode implements SpecificationRuleCode {

  UNDEFINED_FIELD("undefinedField"),
  MISSING_FIELD("missingField"),
  INVALID_FIELD_TAG("invalidFieldTag"),
  NON_REPEATABLE_1XX_FIELD("nonRepeatable1XXField"),
  NON_REPEATABLE_REQUIRED_1XX_FIELD("nonRepeatableRequired1XXField"),
  NON_REPEATABLE_FIELD("nonRepeatableField"),
  INVALID_INDICATOR("invalidIndicator");

  private final String code;

  MarcRuleCode(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }
}
