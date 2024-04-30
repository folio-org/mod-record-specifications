package org.folio.support;

import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiEndpoints {

  public static final String SPECIFICATION_PATH = "/specification-storage/specifications";

  public static String specificationsPath() {
    return SPECIFICATION_PATH;
  }

  public static String specificationsPath(QueryParams queryParams) {
    return addQueryParams(specificationsPath(), queryParams);
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
