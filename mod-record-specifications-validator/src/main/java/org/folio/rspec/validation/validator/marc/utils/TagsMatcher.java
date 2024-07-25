package org.folio.rspec.validation.validator.marc.utils;

import java.util.regex.Pattern;

public final class TagsMatcher {
  private static final Pattern TAG_1XX_PATTERN = Pattern.compile("^1\\d{2}$");

  private TagsMatcher() {
    throw new IllegalStateException("Utility class");
  }

  public static boolean matches1xx(String tag) {
    return TAG_1XX_PATTERN.matcher(tag).matches();
  }
}
