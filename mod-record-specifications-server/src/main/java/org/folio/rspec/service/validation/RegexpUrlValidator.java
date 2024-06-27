/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.folio.rspec.service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.internal.util.DomainNameUtil;

public class RegexpUrlValidator implements ConstraintValidator<URL, CharSequence> {

  @SuppressWarnings({"java:S5852", "java:S5998"})
  private static final Pattern URL_PATTERN = Pattern.compile(
    "^((http|https)://)" // protocol
    + "(localhost|"
    + "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|((\\d{1,3}\\.){3}\\d{1,3})))" // domain name or IP or localhost
    + "(:\\d+)?(/[-a-z\\d%_.~+]*)*" // port and path
    + "(\\?[;&a-z\\d%_.~+=-]*)?" // query string
    + "(#[-a-z\\d_]*)?$", // fragment locator
    Pattern.CASE_INSENSITIVE);

  @Override
  public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
    if (value == null) {
      return true;
    }

    var trimmedValue = value.toString().trim();

    if (trimmedValue.isEmpty()) {
      return false;
    }

    ValueHolder values = parseUrl(trimmedValue);
    if (values == null) {
      return false;
    }

    if (values.protocol() == null || values.protocol().isEmpty()) {
      return false;
    }

    return values.host() != null && DomainNameUtil.isValidDomainAddress(values.host());
  }

  private ValueHolder parseUrl(String stringUrl) {
    ValueHolder valueHolder = null;

    Matcher regexpMatcher = URL_PATTERN.matcher(stringUrl);
    if (regexpMatcher.matches()) {
      String protocol = regexpMatcher.group(1);
      String host = regexpMatcher.group(3);

      valueHolder = new ValueHolder(protocol, host);
    }

    return valueHolder;
  }

  private record ValueHolder(String protocol, String host) { }
}
