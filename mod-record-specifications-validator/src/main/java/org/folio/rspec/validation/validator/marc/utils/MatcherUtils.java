package org.folio.rspec.validation.validator.marc.utils;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MatcherUtils {
  public static final Pattern TAG_PATTERN = Pattern.compile("^\\d{3}$");
  private static final Pattern TAG_1XX_PATTERN = Pattern.compile("^1\\d{2}$");

  public static boolean matchesValidTag(String tag) {
    return tag != null && TAG_PATTERN.matcher(tag).matches();
  }

  public static boolean matches1xxTag(String tag) {
    return tag != null && TAG_1XX_PATTERN.matcher(tag).matches();
  }

  public static boolean matchesValidIndicator(char symbol) {
    return symbol == '#' || symbol >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'z';
  }
}
