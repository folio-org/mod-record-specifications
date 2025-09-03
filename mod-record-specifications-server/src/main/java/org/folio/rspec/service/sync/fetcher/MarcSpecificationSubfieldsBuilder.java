package org.folio.rspec.service.sync.fetcher;

import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.CODE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.DEPRECATED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.LABEL_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REPEATABLE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REQUIRED_PROP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcSpecificationSubfieldsBuilder {

  private static final Pattern SUBFIELD_PATTERN = Pattern.compile(
    ("^\\$(?<%s>.) - (?<%s>.+?)"                  // Code and label
      + "(?:\\s*\\((?<%s>R|NR)\\))?"              // Optional (R|NR)
      + "(?:\\s*\\([^)]*\\))?"                    // Optional cataloging level (ignored) (e.g. (MU VM SE))
      + "(?:\\s*\\[(?<%s>OBSOLETE)\\])?"          // Optional [OBSOLETE]
      + "(?:\\s*\\[[^\\]]+\\])?"                  // Optional tags (ignored) (e.g. [LOCAL])
      + "\\s*$")
      .formatted(CODE_PROP, LABEL_PROP, REPEATABLE_PROP, DEPRECATED_PROP)
  );
  private static final String NON_REPEATABLE_SIGN = "NR";

  private final ObjectMapper objectMapper;

  public ArrayNode build(List<String> lines) {
    var subfieldsArray = objectMapper.createArrayNode();
    for (String line : lines) {
      var subfieldMatcher = SUBFIELD_PATTERN.matcher(line);
      if (subfieldMatcher.find()) {
        var subfieldObject = objectMapper.createObjectNode();
        subfieldObject.put(CODE_PROP, getCode(subfieldMatcher));
        subfieldObject.put(LABEL_PROP, getLabel(subfieldMatcher));
        subfieldObject.put(REPEATABLE_PROP, isRepeatable(subfieldMatcher));
        subfieldObject.put(REQUIRED_PROP, false);
        subfieldObject.put(DEPRECATED_PROP, isDeprecated(subfieldMatcher));
        subfieldsArray.add(subfieldObject);
      }
    }
    return subfieldsArray;
  }

  private String getCode(Matcher subfieldMatcher) {
    return subfieldMatcher.group(CODE_PROP);
  }

  private String getLabel(Matcher subfieldMatcher) {
    return subfieldMatcher.group(LABEL_PROP);
  }

  private boolean isRepeatable(Matcher subfieldMatcher) {
    return !NON_REPEATABLE_SIGN.equals(subfieldMatcher.group(REPEATABLE_PROP));
  }

  private boolean isDeprecated(Matcher subfieldMatcher) {
    return subfieldMatcher.group(DEPRECATED_PROP) != null;
  }
}
