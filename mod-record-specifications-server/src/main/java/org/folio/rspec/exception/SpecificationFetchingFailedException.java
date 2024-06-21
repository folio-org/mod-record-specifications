package org.folio.rspec.exception;

public class SpecificationFetchingFailedException extends RuntimeException {

  private static final String MSG = "Specification fetching failed.";

  public SpecificationFetchingFailedException() {
    super(MSG);
  }

  public SpecificationFetchingFailedException(Throwable cause) {
    super(MSG, cause);
  }
}
