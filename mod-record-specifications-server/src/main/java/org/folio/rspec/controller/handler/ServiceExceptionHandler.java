package org.folio.rspec.controller.handler;

import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface ServiceExceptionHandler {

  ResponseEntity<ErrorCollection> handleException(Exception e);

  boolean canHandle(Exception e);

  static ResponseEntity<ErrorCollection> fallback(Exception e) {
    ErrorCollection errorCollection = new ErrorCollection();
    errorCollection.addErrorsItem(fromErrorCode(ErrorCode.UNEXPECTED).message("Unexpected error: " + e.getMessage()));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorCollection);
  }

  static Error fromErrorCode(ErrorCode errorCode) {
    return new Error().code(errorCode.getCode()).type(errorCode.getErrorType());
  }
}
