package org.folio.rspec.validation.validator.marc.model;

public record MarcIndicator(Reference reference, Character value) {

  public Integer order() {
    return reference().getIndicatorIndex();
  }
}
