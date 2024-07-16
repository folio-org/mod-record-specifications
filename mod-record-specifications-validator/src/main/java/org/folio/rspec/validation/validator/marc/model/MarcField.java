package org.folio.rspec.validation.validator.marc.model;

public interface MarcField {

  Reference reference();

  boolean hasValue();

  default String tag() {
    return reference().getTag();
  }
}
