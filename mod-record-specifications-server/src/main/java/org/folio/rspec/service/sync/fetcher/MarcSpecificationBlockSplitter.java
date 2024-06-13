package org.folio.rspec.service.sync.fetcher;

import static java.util.regex.Pattern.compile;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

@Component
public class MarcSpecificationBlockSplitter {

  private static final Pattern FIELD_BLOCK_SPLIT_PATTERN = compile("(?=(\\d{3}) - (.*) \\((.*)\\)( \\[OBSOLETE])?)");
  private static final Pattern LINE_SPLIT_PATTERN = compile("\n");

  private static final String COMMENT_LINE_PREFIX = "--";
  private static final String INDICATORS_LINE_PREFIX = "Indicators";
  private static final String SUBFIELDS_LINE_PREFIX = "Subfield Code";

  public List<List<String>> split(String textBlock) {
    return Arrays.stream(FIELD_BLOCK_SPLIT_PATTERN.split(textBlock))
      .map(this::blockToLines)
      .toList();
  }

  public int getIndicatorsLineNumber(List<String> lines) {
    return getLineNumberMatches(lines, this::isIndicatorsLine);
  }

  public int getSubfieldLineNumber(List<String> lines) {
    return getLineNumberMatches(lines, this::isSubfieldsLine);
  }

  private int getLineNumberMatches(List<String> lines, Predicate<String> predicate) {
    for (int i = 0; i < lines.size(); i++) {
      if (predicate.test(lines.get(i))) {
        return i + 1;
      }
    }
    return ArrayUtils.INDEX_NOT_FOUND;
  }

  private List<String> blockToLines(String strBlock) {
    return Arrays.stream(LINE_SPLIT_PATTERN.split(strBlock))
      .map(String::trim)
      .filter(string -> !isCommentLine(string))
      .toList();
  }

  private boolean isCommentLine(String string) {
    return string.startsWith(COMMENT_LINE_PREFIX);
  }

  private boolean isIndicatorsLine(String line) {
    return line.startsWith(INDICATORS_LINE_PREFIX);
  }

  private boolean isSubfieldsLine(String line) {
    return line.startsWith(SUBFIELDS_LINE_PREFIX);
  }

}
