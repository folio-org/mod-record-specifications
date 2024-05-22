package org.folio.support;

import java.util.UUID;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiEndpoints {

  public static final String SPECIFICATIONS_PATH = "/specification-storage/specifications";
  public static final String SPECIFICATION_RULES_PATH = SPECIFICATIONS_PATH + "/%s/rules";
  public static final String SPECIFICATION_RULE_PATH = SPECIFICATION_RULES_PATH + "/%s";
  public static final String SPECIFICATION_FIELDS_PATH = SPECIFICATIONS_PATH + "/%s/fields";

  public static String specificationsPath() {
    return SPECIFICATIONS_PATH;
  }

  public static String specificationsPath(QueryParams queryParams) {
    return addQueryParams(specificationsPath(), queryParams);
  }

  public static String specificationRulesPath(String specId) {
    return SPECIFICATION_RULES_PATH.formatted(specId);
  }

  public static String specificationRulesPath(UUID specId) {
    return specificationRulesPath(specId.toString());
  }

  public static String specificationRulePath(String specId, String ruleId) {
    return SPECIFICATION_RULE_PATH.formatted(specId, ruleId);
  }

  public static String specificationRulePath(UUID specId, UUID ruleId) {
    return specificationRulePath(specId.toString(), ruleId.toString());
  }

  public static String specificationFieldsPath(String specId) {
    return SPECIFICATION_FIELDS_PATH.formatted(specId);
  }

  public static String specificationFieldsPath(UUID specId) {
    return specificationFieldsPath(specId.toString());
  }

  private static String addQueryParams(String path, QueryParams queryParams) {
    if (queryParams.isEmpty()) {
      return path;
    }
    var queryParamString = queryParams.stream()
      .map(param -> param.getKey() + "=" + param.getValue())
      .collect(Collectors.joining("&"));
    return path + "?" + queryParamString;
  }

}
