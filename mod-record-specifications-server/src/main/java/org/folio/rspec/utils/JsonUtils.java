package org.folio.rspec.utils;

import lombok.experimental.UtilityClass;
import tools.jackson.databind.JsonNode;

@UtilityClass
public class JsonUtils {

  public static String getText(JsonNode jsonNode, String fieldName) {
    if (jsonNode == null || fieldName == null) {
      return null;
    }
    return jsonNode.get(fieldName).asString(null);
  }

  public static boolean getBoolean(JsonNode jsonNode, String fieldName) {
    if (jsonNode == null || fieldName == null) {
      return false;
    }
    return jsonNode.get(fieldName).asBoolean(false);
  }

  public static int getInt(JsonNode jsonNode, String fieldName) {
    if (jsonNode == null || fieldName == null) {
      return -1;
    }
    return jsonNode.get(fieldName).asInt(-1);
  }
}
