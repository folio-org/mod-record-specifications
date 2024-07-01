package org.folio.rspec.service.sync.fetcher;

import static org.apache.commons.lang3.ArrayUtils.INDEX_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.CODES_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.CODE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.LABEL_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.ORDER_PROP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcSpecificationIndicatorBuilder {

  private static final String FIRST_INDICATOR_PREFIX = "First";
  private static final String SECOND_INDICATOR_PREFIX = "Second";
  private static final String DEPRECATED_SIGN = "OBSOLETE";
  private static final String NUMBER_RANGE_SIGN = "0-9";
  private static final char DASH = '-';
  private static final char UNDEFINED = '#';
  private static final char SLASH = '/';

  private final ObjectMapper objectMapper;

  public ArrayNode build(List<String> lines) {
    var indicatorsArray = objectMapper.createArrayNode();
    indicatorsArray.add(buildIndicator(lines, 1));
    indicatorsArray.add(buildIndicator(lines, 2));
    return indicatorsArray;
  }

  private ObjectNode buildIndicator(List<String> lines, int indOrder) {
    return switch (indOrder) {
      case 1 -> buildIndicator(lines, indOrder, getIndicatorLineNumber(lines, FIRST_INDICATOR_PREFIX));
      case 2 -> buildIndicator(lines, indOrder, getIndicatorLineNumber(lines, SECOND_INDICATOR_PREFIX));
      default -> throw new IllegalStateException("Unexpected value: " + indOrder);
    };
  }

  private ObjectNode buildIndicator(List<String> lines, int indOrder, int startLine) {
    var indicatorObject = objectMapper.createObjectNode();
    indicatorObject.put(LABEL_PROP, getIndicatorLabel(lines.get(startLine)));
    indicatorObject.put(ORDER_PROP, indOrder);
    indicatorObject.set(CODES_PROP, getIndicatorCodes(lines, startLine));

    return indicatorObject;
  }

  private ArrayNode getIndicatorCodes(List<String> lines, int startLine) {
    var codesArray = objectMapper.createArrayNode();
    for (int i = startLine + 1; i < lines.size(); i++) {
      var line = lines.get(i);
      if (isNextIndicatorLine(line)) {
        break;
      }
      if (isNumberRangeCode(line)) {
        var label = getNumRangeCodeLabel(line);
        for (int j = 0; j <= 9; j++) {
          var codeObject = toCodeObject(String.valueOf(j), label);
          codesArray.add(codeObject);
        }
      } else {
        var code = getCode(line);
        var label = getStandardCodeLabel(line);
        var codeObject = toCodeObject(code, label);
        codesArray.add(codeObject);
      }
    }
    return codesArray;
  }

  private String getCode(String line) {
    var code = line.charAt(0);
    return String.valueOf(code == UNDEFINED ? SLASH : code);
  }

  private String getStandardCodeLabel(String line) {
    return removeStart(line.substring(1).trim(), DASH).trim();
  }

  private String getNumRangeCodeLabel(String line) {
    return removeStart(removeStart(line, NUMBER_RANGE_SIGN).trim(), DASH).trim();
  }

  private boolean isNextIndicatorLine(String line) {
    return line.startsWith(FIRST_INDICATOR_PREFIX) || line.startsWith(SECOND_INDICATOR_PREFIX);
  }

  private String getIndicatorLabel(String line) {
    return line.substring(line.indexOf(DASH) + 1).trim();
  }

  private boolean isNumberRangeCode(String line) {
    return line.startsWith(NUMBER_RANGE_SIGN);
  }

  private ObjectNode toCodeObject(String code, String label) {
    var codeObject = objectMapper.createObjectNode();
    codeObject.put(CODE_PROP, code);
    codeObject.put(LABEL_PROP, label);
    return codeObject;
  }

  private int getIndicatorLineNumber(List<String> lines, String indNumPrefix) {
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (line.startsWith(indNumPrefix) && !line.contains(DEPRECATED_SIGN)) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }
}
