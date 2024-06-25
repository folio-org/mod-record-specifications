package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_REQUEST_PARAMETER;

import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
  private final ConstraintViolationResolver constraintViolationResolver;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (MethodArgumentNotValidException) e;
    List<Error> errorList = new ArrayList<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errorList.addAll(createErrorsFromFieldError(fieldError));
    }
    return ResponseEntity.badRequest().body(new ErrorCollection().errors(errorList));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentNotValidException;
  }

  private Collection<Error> createErrorsFromFieldError(FieldError fieldError) {
    if (fieldError.contains(ConstraintViolation.class)) {
      var violation = fieldError.unwrap(ConstraintViolation.class);
      return constraintViolationResolver.processViolation(violation, INVALID_REQUEST_PARAMETER);
    }
    var error = fromErrorCode(INVALID_REQUEST_PARAMETER);
    error.setMessage(translationService.format(fieldError.getCodes(), FIELD_MSG_ARG, fieldError.getField()));
    var parameter = new Parameter().key(fieldError.getField()).value(String.valueOf(fieldError.getRejectedValue()));
    error.addParametersItem(parameter);
    return Collections.singleton(error);
  }
}
