package org.folio.rspec.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {

  public static String getText(JsonNode jsonNode, String fieldName) {
    if (jsonNode == null || fieldName == null) {
      return null;
    }
    return jsonNode.get(fieldName).asText(null);
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
