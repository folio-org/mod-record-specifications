package org.folio.rspec.exception;

public class ResourceValidationFailedException extends RuntimeException {

  public ResourceValidationFailedException(String message) {
    super(message);
  }
}
