package org.folio.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
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

  public static SpecificationDto getSpecificationWithTags(String... tags) {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(fieldDefinitionsTags(tags));
  }

  public static SpecificationDto getSpecificationWithIndicators() {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(indicatorsFieldDefinitions());
  }

  private static List<SpecificationFieldDto> commonFieldDefinitions() {
    List<SpecificationFieldDto> fields = new ArrayList<>();
    fields.add(requiredNonRepeatableField("000"));
    fields.add(requiredNonRepeatableField("001"));
    fields.add(defaultField("005"));
    fields.add(defaultField("006"));
    fields.add(defaultField("007"));
    fields.add(defaultField("008"));
    fields.add(defaultField("010"));
    return fields;
  }

  private static List<SpecificationFieldDto> indicatorsFieldDefinitions() {
    List<SpecificationFieldDto> fields = new ArrayList<>();
    fields.add(requiredNonRepeatableField("000"));
    fields.add(defaultFieldWithIndicator("010"));
    fields.add(defaultFieldWithIndicator("035"));
    fields.add(defaultFieldWithIndicator("047"));
    fields.add(defaultFieldWithIndicator("100"));
    fields.add(defaultFieldWithIndicator("245"));
    fields.add(defaultFieldWithIndicator("650"));
    return fields;
  }

  private static List<SpecificationFieldDto> fieldDefinitions() {
    List<SpecificationFieldDto> fields = commonFieldDefinitions();
    fields.add(defaultField("035"));
    fields.add(nonRepeatableField("100"));
    fields.add(requiredField("245"));
    fields.add(requiredField("889"));
    fields.add(requiredNonRepeatableField("650"));
    return fields;
  }

  private static List<SpecificationFieldDto> fieldDefinitionsTags(String[] tags) {
    List<SpecificationFieldDto> fields = commonFieldDefinitions();
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

  private static SpecificationFieldDto defaultFieldWithIndicator(String tag) {
    return defaultField(tag).indicators(List.of(getIndicator(), getIndicator()));
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

  private static FieldIndicatorDto getIndicator() {
    return new FieldIndicatorDto().id(UUID.randomUUID()).addCodesItem(new IndicatorCodeDto().code("code"));
  }
}
