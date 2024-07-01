package org.folio.rspec.service.sync.fetcher;

import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.DEPRECATED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.LABEL_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REPEATABLE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REQUIRED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.TAG_PROP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcSpecificationFieldBuilder {

  private static final Pattern FIELD_PATTERN = Pattern.compile(
    "(?<%s>\\d{3}) - (?<%s>.{1,500})\\s?\\((?<%s>.{1,100})\\)[\\s-]{0,10}(?<%s>\\[OBSOLETE])?"
      .formatted(TAG_PROP, LABEL_PROP, REPEATABLE_PROP, DEPRECATED_PROP));
  private static final String NON_REPEATABLE_SIGN = "NR";

  private final MarcSpecificationFieldLabelModifier labelModifier;
  private final ObjectMapper objectMapper;

  public ObjectNode build(String line) {
    var jsonObject = objectMapper.createObjectNode();
    var fieldMatcher = FIELD_PATTERN.matcher(line);
    if (fieldMatcher.lookingAt()) {
      var deprecated = isDeprecated(fieldMatcher);
      jsonObject.put(TAG_PROP, getTag(fieldMatcher));
      jsonObject.put(LABEL_PROP, getLabel(fieldMatcher, deprecated));
      jsonObject.put(REPEATABLE_PROP, isRepeatable(fieldMatcher));
      jsonObject.put(REQUIRED_PROP, false);
      jsonObject.put(DEPRECATED_PROP, deprecated);
    }
    return jsonObject;
  }

  private static String getTag(Matcher fieldMatcher) {
    return fieldMatcher.group(TAG_PROP);
  }

  private String getLabel(Matcher fieldMatcher, boolean deprecated) {
    return deprecated
           ? labelModifier.modify(fieldMatcher.group(LABEL_PROP)) + " [OBSOLETE]"
           : labelModifier.modify(fieldMatcher.group(LABEL_PROP));
  }

  private boolean isRepeatable(Matcher fieldMatcher) {
    return !NON_REPEATABLE_SIGN.equals(fieldMatcher.group(REPEATABLE_PROP));
  }

  private boolean isDeprecated(Matcher fieldMatcher) {
    return fieldMatcher.group(DEPRECATED_PROP) != null;
  }
}
