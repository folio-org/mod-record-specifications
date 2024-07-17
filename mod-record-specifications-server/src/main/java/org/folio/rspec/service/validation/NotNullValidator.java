package org.folio.rspec.service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;

public class NotNullValidator implements ConstraintValidator<NotNull, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value instanceof String stringValue) {
      return StringUtils.isNotBlank(stringValue);
    } else if (value instanceof Collection<?> collectionValue) {
      return !collectionValue.isEmpty();
    }
    return value != null;
  }
}
