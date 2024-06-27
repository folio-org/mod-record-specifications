package org.folio.rspec.controller.handler;

import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

@Component
public class HttpMessageNotReadableExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var errorCollection = new ErrorCollection();
    errorCollection.addErrorsItem(ServiceExceptionHandler.fromErrorCode(ErrorCode.UNEXPECTED).message(e.getMessage()));
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof HttpMessageNotReadableException;
  }
}
