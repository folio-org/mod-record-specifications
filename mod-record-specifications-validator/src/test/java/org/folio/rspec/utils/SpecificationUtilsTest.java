package org.folio.rspec.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class SpecificationUtilsTest {

  @Test
  void enabledRules_returnsListOfEnabledRules() {
    SpecificationRuleDto enabledRule = new SpecificationRuleDto();
    enabledRule.setEnabled(true);
    enabledRule.setCode("rule1");

    SpecificationRuleDto disabledRule = new SpecificationRuleDto();
    disabledRule.setEnabled(false);
    disabledRule.setCode("rule2");

    SpecificationDto specification = new SpecificationDto();
    specification.setRules(List.of(enabledRule, disabledRule));

    List<String> result = SpecificationUtils.enabledRules(specification);

    assertEquals(1, result.size());
    assertEquals("rule1", result.get(0));
  }

  @Test
  void ruleIsEnabled_returnsTrueIfRuleIsEnabled() {
    SpecificationRuleDto rule = new SpecificationRuleDto();
    rule.setEnabled(true);
    rule.setCode("rule1");

    SpecificationDto specification = new SpecificationDto();
    specification.setRules(List.of(rule));

    assertTrue(SpecificationUtils.ruleIsEnabled("rule1", specification));
  }

  @Test
  void requiredFields_returnsRequiredFields() {
    SpecificationFieldDto requiredField = new SpecificationFieldDto();
    requiredField.setRequired(true);
    requiredField.setTag("tag1");

    SpecificationFieldDto optionalField = new SpecificationFieldDto();
    optionalField.setRequired(false);
    optionalField.setTag("tag2");

    SpecificationDto specification = new SpecificationDto();
    specification.setFields(List.of(requiredField, optionalField));

    Map<String, SpecificationFieldDto> result = SpecificationUtils.requiredFields(specification);

    assertEquals(1, result.size());
    assertTrue(result.containsKey("tag1"));
  }

  @Test
  void findField_returnsField() {
    SpecificationFieldDto field = new SpecificationFieldDto();
    field.setTag("tag1");

    SpecificationDto specification = new SpecificationDto();
    specification.setFields(List.of(field));

    Optional<SpecificationFieldDto> result = SpecificationUtils.findField(specification, "tag1");

    assertTrue(result.isPresent());
    assertEquals("tag1", result.get().getTag());
  }

  @Test
  void requiredSubfields_returnsRequiredSubfields() {
    Map<Character, SubfieldDto> result = SpecificationUtils.requiredSubfields(
      List.of(
        getSubfieldDto('a', true, true),
        getSubfieldDto('b', false, true),
        getSubfieldDto('c', true, true)));

    assertEquals(2, result.size());
    assertTrue(result.containsKey('a'));
    assertTrue(result.containsKey('c'));
  }

  @Test
  void nonRepeatableSubfields_returnsNonRepeatableSubfields() {
    Map<Character, SubfieldDto> result = SpecificationUtils.nonRepeatableSubfields(
      List.of(
        getSubfieldDto('a', true, true),
        getSubfieldDto('b', false, false),
        getSubfieldDto('c', true, false)));

    assertEquals(2, result.size());
    assertTrue(result.containsKey('b'));
    assertTrue(result.containsKey('c'));
  }

  private SubfieldDto getSubfieldDto(Character code, boolean isRequired, boolean isRepeatable) {
    return new SubfieldDto().code(code.toString()).required(isRequired).repeatable(isRepeatable);
  }
}
