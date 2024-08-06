package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RuleValidatorsProviderTest {
  private static final MarcRuleCode FIELD_RULE_CODE = MarcRuleCode.NON_REPEATABLE_FIELD;
  private static final MarcRuleCode FIELD_SET_RULE_CODE = MarcRuleCode.UNDEFINED_FIELD;

  @Mock
  TranslationProvider translationProvider;

  @Test
  void getValidator_whenUnknownRuleCode_shouldReturnNull() {
    // provide not field rule code
    var fieldValidator = RuleValidatorsProvider.getFieldValidator(FIELD_SET_RULE_CODE, translationProvider);

    // provide not field set rule code
    var fieldSetValidator = RuleValidatorsProvider.getFieldSetValidator(FIELD_RULE_CODE, translationProvider);

    assertNull(fieldValidator);
    assertNull(fieldSetValidator);
  }

  @Test
  void getValidator_whenFieldRuleCode_shouldReturnFieldValidator() {
    var validator = RuleValidatorsProvider.getFieldValidator(FIELD_RULE_CODE, translationProvider);

    assertNotNull(validator);
    assertInstanceOf(MarcFieldNonRepeatableFieldRuleValidator.class, validator);
  }

  @Test
  void getValidator_whenFieldSetRuleCode_shouldReturnFieldSetValidator() {
    var validator = RuleValidatorsProvider.getFieldSetValidator(FIELD_SET_RULE_CODE, translationProvider);

    assertNotNull(validator);
    assertInstanceOf(MarcFieldUndefinedFieldRuleValidator.class, validator);
  }
}
