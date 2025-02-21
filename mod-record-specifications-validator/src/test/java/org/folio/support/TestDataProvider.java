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
import org.folio.rspec.domain.dto.SubfieldDto;
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

  public static SpecificationDto getSpecificationWithSubfields() {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(subfieldDefinitions());
  }

  public static SpecificationDto getSpecificationWithNonRepeatableSubfields() {
    return new SpecificationDto()
      .id(UUID.randomUUID())
      .family(Family.MARC)
      .profile(FamilyProfile.BIBLIOGRAPHIC)
      .rules(allEnabledRules())
      .fields(getNonRepeatableSubfieldDefinitions());
  }

  private static List<SpecificationFieldDto> getNonRepeatableSubfieldDefinitions() {
    return List.of(requiredNonRepeatableField("000"),
      defaultFieldWithNonRepeatableSubfields("010"),
      defaultFieldWithNonRepeatableSubfields("035"),
      defaultFieldWithNonRepeatableSubfields("047"),
      defaultFieldWithNonRepeatableSubfields("100"),
      defaultFieldWithNonRepeatableSubfields("245"),
      defaultFieldWithNonRepeatableSubfields("650"));
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
    fields.add(defaultField("010"));
    fields.add(defaultField("035"));
    fields.add(defaultField("047"));
    fields.add(defaultField("100"));
    fields.add(defaultField("130"));
    fields.add(defaultField("245"));
    fields.add(defaultField("650"));
    return fields;
  }

  private static List<SpecificationFieldDto> subfieldDefinitions() {
    return List.of(requiredNonRepeatableField("000"),
      defaultFieldWithSubfields("010"),
      defaultFieldWithSubfields("035"),
      defaultFieldWithSubfields("047"),
      defaultFieldWithSubfields("100"),
      defaultFieldWithSubfields("245"),
      defaultFieldWithSubfields("246"),
      defaultFieldWithSubfields("650"));
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

  private static SpecificationFieldDto defaultFieldWithSubfields(String tag) {
    return defaultField(tag).subfields(List.of(
      getSubfield("a", true, true),
      getSubfield("d", true, true),
      getSubfield("k", false, true),
      getSubfield("s", false, true),
      getSubfield("0", false, true)));
  }

  private static SpecificationFieldDto defaultFieldWithNonRepeatableSubfields(String tag) {
    return defaultField(tag).subfields(List.of(
      getSubfield("a", true, true),
      getSubfield("d", true, false),
      getSubfield("k", false, true),
      getSubfield("s", false, true),
      getSubfield("c", false, true),
      getSubfield("w", false, false)));
  }

  private static SpecificationFieldDto fieldDefinition(String tag, boolean required, boolean deprecated,
                                                       boolean repeatable) {
    return new SpecificationFieldDto()
      .id(UUID.randomUUID())
      .tag(tag)
      .indicators(getFieldIndicatorDtoListByTag(tag))
      .subfields(List.of(
        getSubfield("a", false, true),
        getSubfield("b", false, true),
        getSubfield("c", false, true),
        getSubfield("d", false, true),
        getSubfield("z", false, true),
        getSubfield("0", false, true),
        getSubfield("2", false, true),
        getSubfield("9", false, true)))
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

  private static List<FieldIndicatorDto> getFieldIndicatorDtoListByTag(String tag) {
    return switch (tag) {
      case "010", "035" -> List.of(
        getFieldIndicatorDto(1, List.of("#")),
        getFieldIndicatorDto(2, List.of("#")));
      case "047" -> List.of(
        getFieldIndicatorDto(1, List.of("#")),
        getFieldIndicatorDto(2, List.of("#", "7")));
      case "100" -> List.of(
        getFieldIndicatorDto(1, List.of("0", "1", "2", "3")),
        getFieldIndicatorDto(2, List.of("#")));
      case "130" -> List.of(
        getFieldIndicatorDto(1, List.of("#", "0", "1", "2", "3", "4", "5", "6", "7")),
        getFieldIndicatorDto(2, List.of("#")));
      case "245", "246" -> List.of(
        getFieldIndicatorDto(1, List.of("0", "1")),
        getFieldIndicatorDto(2, List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
      case "650" -> List.of(
        getFieldIndicatorDto(1, List.of("#", "0", "1", "2")),
        getFieldIndicatorDto(2, List.of("0", "1", "2", "3", "4", "5", "6", "7")));
      default -> null;
    };
  }

  private static FieldIndicatorDto getFieldIndicatorDto(int order, List<String> codes) {
    var indicatorCodeDto = codes.stream().map(code -> new IndicatorCodeDto().code(code)).toList();
    return new FieldIndicatorDto().order(order).codes(indicatorCodeDto);
  }

  private static SubfieldDto getSubfield(String code, boolean required, boolean repeatable) {
    return new SubfieldDto()
      .id(UUID.randomUUID())
      .required(required)
      .deprecated(false)
      .repeatable(repeatable)
      .code(code);
  }
}
