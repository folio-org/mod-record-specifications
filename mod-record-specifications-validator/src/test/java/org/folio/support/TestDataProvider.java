package org.folio.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.validation.validator.marc.model.MarcRuleCode;

public class TestDataProvider {

  public static SpecificationDto getSpecification() {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(fieldDefinitions());
  }

  private static List<SpecificationFieldDto> fieldDefinitions() {
    return List.of(
      requiredNonRepeatableField("000"),
      requiredNonRepeatableField("001"),
      defaultField("005"),
      defaultField("006"),
      defaultField("007"),
      defaultField("008"),
      defaultField("010"),
      defaultField("035"),
      nonRepeatableField("100"),
      requiredField("245"),
      requiredField("889"),
      requiredNonRepeatableField("650")
    );
  }

  public static SpecificationDto getSpecification1xx(String... tags) {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(fieldDefinitions1xx(tags));
  }

  private static List<SpecificationFieldDto> fieldDefinitions1xx(String[] tags) {
    List<SpecificationFieldDto> fields = new ArrayList<>();
    fields.add(requiredNonRepeatableField("000"));
    fields.add(requiredNonRepeatableField("001"));
    fields.add(defaultField("005"));
    fields.add(defaultField("006"));
    fields.add(defaultField("007"));
    fields.add(defaultField("008"));
    fields.add(defaultField("010"));

    Arrays.stream(tags).forEach(tag -> fields.add(nonRepeatableField(tag)));

    return fields;
  }

  private static SpecificationFieldDto requiredField(String tag) {
    return fieldDefinition(tag, true, false, true);
  }

  private static SpecificationFieldDto requiredNonRepeatableField(String tag) {
    return fieldDefinition(tag, true, false, false);
  }

  private static SpecificationFieldDto nonRepeatableField(String tag) {
    return fieldDefinition(tag, false, false, false);
  }

  private static SpecificationFieldDto defaultField(String tag) {
    return fieldDefinition(tag, false, false, true);
  }

  private static SpecificationFieldDto fieldDefinition(String tag, boolean required, boolean deprecated,
                                                       boolean repeatable) {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .tag(tag)
      .required(required)
      .deprecated(deprecated)
      .repeatable(repeatable);
  }

  private static List<SpecificationRuleDto> allEnabledRules() {
    return Arrays.stream(MarcRuleCode.values()).map(TestDataProvider::enabledRule).toList();
  }

  private static SpecificationRuleDto enabledRule(MarcRuleCode ruleCode) {
    return new SpecificationRuleDto().id(UUID.randomUUID()).code(ruleCode.getCode()).enabled(true);
  }
}
