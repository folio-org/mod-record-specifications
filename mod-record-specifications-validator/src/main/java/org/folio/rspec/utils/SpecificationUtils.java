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
}
