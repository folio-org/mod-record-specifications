package org.folio.rspec.validation.validator.marc.model;

public record MarcSubfield(Reference reference, String value) {

  public Character code() {
    return reference.getSubfield();
  }
}
