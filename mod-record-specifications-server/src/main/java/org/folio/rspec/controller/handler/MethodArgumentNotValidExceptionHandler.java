package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_REQUEST_PARAMETER;

import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
public class MethodArgumentNotValidExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (MethodArgumentNotValidException) e;
    var errorCollection = new ErrorCollection();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      var error = fromErrorCode(INVALID_REQUEST_PARAMETER);
      error.setMessage(fieldError.getDefaultMessage());
      var parameter = new Parameter().key(fieldError.getField()).value(String.valueOf(fieldError.getRejectedValue()));
      error.addParametersItem(parameter);
      errorCollection.addErrorsItem(error);
    }
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentNotValidException;
  }
}
