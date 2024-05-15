package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.errorCollection;
import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.RESOURCE_NOT_FOUND;

import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResourceNotFoundExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var error = fromErrorCode(RESOURCE_NOT_FOUND).message(e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorCollection(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ResourceNotFoundException;
  }
}
