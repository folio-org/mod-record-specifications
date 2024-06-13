package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_REQUEST_PARAMETER;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Log4j2
@Component
@RequiredArgsConstructor
public class MethodArgumentNotValidExceptionHandler implements ServiceExceptionHandler {

  private static final String FIELD_MSG_ARG = "field";

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (MethodArgumentNotValidException) e;
    var errorCollection = new ErrorCollection();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errorCollection.addErrorsItem(createErrorFromFieldError(fieldError));
    }
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentNotValidException;
  }

  private Error createErrorFromFieldError(FieldError fieldError) {
    var error = fromErrorCode(INVALID_REQUEST_PARAMETER);
    error.setMessage(translationService.format(fieldError.getCodes(), FIELD_MSG_ARG, fieldError.getField()));
    var parameter = new Parameter().key(fieldError.getField()).value(String.valueOf(fieldError.getRejectedValue()));
    error.addParametersItem(parameter);
    return error;
  }
}