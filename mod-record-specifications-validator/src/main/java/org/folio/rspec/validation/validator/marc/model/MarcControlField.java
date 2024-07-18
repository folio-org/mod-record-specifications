package org.folio.rspec.validation.validator.marc.model;

import org.apache.commons.lang3.StringUtils;

public record MarcControlField(Reference reference, String value) implements MarcField {

  @Override
  public boolean hasValue() {
    return StringUtils.isNotBlank(value);
  }
}
