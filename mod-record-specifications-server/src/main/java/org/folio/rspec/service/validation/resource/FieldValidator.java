package org.folio.rspec.service.validation.resource;

import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.exception.ResourceValidationFailedException;
import org.springframework.stereotype.Component;

@Component
public class FieldValidator {

  private static final String ERROR_MESSAGE = "Cannot define %ss for 00X control fields.";
  private static final String CONTROL_FIELD_TAG_START = "00";

  public void validateFieldResourceCreate(Field field, String resource) {
    if (field.getTag().startsWith(CONTROL_FIELD_TAG_START)) {
      throw new ResourceValidationFailedException(resource, ERROR_MESSAGE.formatted(resource));
    }
  }
}
