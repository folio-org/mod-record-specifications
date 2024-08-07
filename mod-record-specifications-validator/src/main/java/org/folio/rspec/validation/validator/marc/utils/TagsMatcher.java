package org.folio.rspec.validation.validator.marc.utils;

import java.util.regex.Pattern;

public final class TagsMatcher {
  public static final Pattern TAG_PATTERN = Pattern.compile("^\\d{3}$");
  private static final Pattern TAG_1XX_PATTERN = Pattern.compile("^1\\d{2}$");

  private TagsMatcher() {
    throw new IllegalStateException("Utility class");
  }

  public static boolean matchesValidTag(String tag) {
    return tag != null && TAG_PATTERN.matcher(tag).matches();
  }

  public static boolean matches1xx(String tag) {
    return tag != null && TAG_1XX_PATTERN.matcher(tag).matches();
  }
}
