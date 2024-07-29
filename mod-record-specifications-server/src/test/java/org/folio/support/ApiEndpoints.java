package org.folio.support;

import java.util.UUID;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiEndpoints {

  public static final String SPECIFICATION_STORAGE_PATH = "/specification-storage";
  public static final String SPECIFICATIONS_PATH = SPECIFICATION_STORAGE_PATH + "/specifications";
  public static final String SPECIFICATION_PATH = SPECIFICATIONS_PATH + "/%s";
  public static final String SPECIFICATION_SYNC_PATH = SPECIFICATIONS_PATH + "/%s/sync";
  public static final String SPECIFICATION_RULES_PATH = SPECIFICATIONS_PATH + "/%s/rules";
  public static final String SPECIFICATION_RULE_PATH = SPECIFICATION_RULES_PATH + "/%s";
  public static final String SPECIFICATION_FIELDS_PATH = SPECIFICATIONS_PATH + "/%s/fields";
  public static final String FIELD_PATH = SPECIFICATION_STORAGE_PATH + "/fields/%s";
  public static final String FIELD_SUBFIELDS_PATH = FIELD_PATH + "/subfields";
  public static final String FIELD_INDICATORS_PATH = FIELD_PATH + "/indicators";
  public static final String INDICATOR_PATH = SPECIFICATION_STORAGE_PATH + "/indicators/%s";
  public static final String INDICATOR_CODES_PATH = INDICATOR_PATH + "/indicator-codes";
  public static final String CODE_PATH = SPECIFICATION_STORAGE_PATH + "/indicator-codes/%s";

  public static String specificationsPath() {
    return SPECIFICATIONS_PATH;
  }

  public static String specificationsPath(QueryParams queryParams) {
    return addQueryParams(specificationsPath(), queryParams);
  }

  public static String specificationPath(UUID specificationId) {
    return SPECIFICATION_PATH.formatted(specificationId);
  }

  public static String specificationPath(UUID specificationId, QueryParams queryParams) {
    return addQueryParams(specificationPath(specificationId), queryParams);
  }

  public static String specificationSyncPath(UUID specId) {
    return specificationSyncPath(specId.toString());
  }

  public static String specificationSyncPath(String specId) {
    return SPECIFICATION_SYNC_PATH.formatted(specId);
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

  public static String fieldPath(String fieldId) {
    return FIELD_PATH.formatted(fieldId);
  }

  public static String fieldPath(UUID fieldId) {
    return fieldPath(fieldId.toString());
  }

  public static String fieldSubfieldsPath(String fieldId) {
    return FIELD_SUBFIELDS_PATH.formatted(fieldId);
  }

  public static String fieldSubfieldsPath(UUID fieldId) {
    return fieldSubfieldsPath(fieldId.toString());
  }

  public static String fieldIndicatorsPath(String fieldId) {
    return FIELD_INDICATORS_PATH.formatted(fieldId);
  }

  public static String fieldIndicatorsPath(UUID fieldId) {
    return fieldIndicatorsPath(fieldId.toString());
  }

  public static String indicatorPath(String indicatorId) {
    return INDICATOR_PATH.formatted(indicatorId);
  }

  public static String indicatorPath(UUID indicatorId) {
    return indicatorPath(indicatorId.toString());
  }

  public static String codePath(String codeId) {
    return CODE_PATH.formatted(codeId);
  }

  public static String codePath(UUID codeId) {
    return codePath(codeId.toString());
  }

  public static String indicatorCodesPath(String indicatorId) {
    return INDICATOR_CODES_PATH.formatted(indicatorId);
  }

  public static String indicatorCodesPath(UUID indicatorId) {
    return indicatorCodesPath(indicatorId.toString());
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
