package org.folio.rspec.exception;

import lombok.Getter;

@Getter
public class ResourceValidationFailedException extends RuntimeException {

  private final String resource;

  public ResourceValidationFailedException(String resource, String message) {
    super(message);
    this.resource = resource;
  }
}
