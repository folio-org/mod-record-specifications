package org.folio.rspec.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SubfieldDto;

@UtilityClass
public class SpecificationUtils {

  public static List<String> enabledRules(SpecificationDto specification) {
    return specification.getRules().stream()
      .filter(ruleDto -> Boolean.TRUE.equals(ruleDto.getEnabled()))
      .map(SpecificationRuleDto::getCode)
      .toList();
  }

  public static boolean ruleIsEnabled(String ruleCode, SpecificationDto specification) {
    return enabledRules(specification).contains(ruleCode);
  }

  public static Map<String, SpecificationFieldDto> requiredFields(SpecificationDto specification) {
    return specification.getFields().stream()
      .filter(fieldDto -> Boolean.TRUE.equals(fieldDto.getRequired()))
      .collect(Collectors.toMap(SpecificationFieldDto::getTag, Function.identity()));
  }

  public static Optional<SpecificationFieldDto> findField(SpecificationDto specification, String tag) {
    return specification.getFields().stream()
      .filter(fieldDto -> tag.equals(fieldDto.getTag()))
      .findFirst();
  }

  public static Map<Character, SubfieldDto> requiredSubfields(List<SubfieldDto> subfieldDto) {
    return subfieldDto == null ? Map.of() : subfieldDto
      .stream()
      .filter(subfield -> Boolean.TRUE.equals(subfield.getRequired()))
      .collect(Collectors.toMap(subfield -> subfield.getCode().charAt(0), Function.identity()));
  }

  public static Map<Character, SubfieldDto> nonRepeatableSubfields(List<SubfieldDto> subfieldDtos) {
    return subfieldDtos == null ? Map.of() : subfieldDtos
      .stream()
      .filter(subfield -> Boolean.FALSE.equals(subfield.getRepeatable()))
      .collect(Collectors.toMap(subfield -> subfield.getCode().charAt(0), Function.identity()));
  }
}
