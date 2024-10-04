package org.folio.rspec.exception;

import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;

public class UpdateRequestProcessingException extends RuntimeException {

  public UpdateRequestProcessingException(String message) {
    super(message);
  }

  public static UpdateRequestProcessingException specificationNotFound(Family family, FamilyProfile profile) {
    return new UpdateRequestProcessingException("Specification for family=%s, profile=%s not found"
      .formatted(family, profile));
  }

}
