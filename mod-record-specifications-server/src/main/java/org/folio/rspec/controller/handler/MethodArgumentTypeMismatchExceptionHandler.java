package org.folio.rspec.controller.handler;

import java.util.Arrays;
import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Component
public class MethodArgumentTypeMismatchExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    MethodArgumentTypeMismatchException exception = (MethodArgumentTypeMismatchException) e;
    Class<?> requiredType = exception.getRequiredType();
    ErrorCollection errorCollection = new ErrorCollection();

    if (requiredType != null && requiredType.isEnum()) {
      String message = buildErrorMessage(exception, requiredType);
      Error code = buildErrorCode(exception, message);
      errorCollection.addErrorsItem(code);
    }

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentTypeMismatchException;
  }

  private String buildErrorMessage(MethodArgumentTypeMismatchException e, Class<?> requiredType) {
    Object[] enumConstants = requiredType.getEnumConstants();
    Throwable rootCause = e.getRootCause();
    return (rootCause != null ? rootCause.getMessage() + ". " : "")
      + "Possible values: " + Arrays.toString(enumConstants);
  }

  private Error buildErrorCode(MethodArgumentTypeMismatchException e, String message) {
    Error code = new Error()
      .message(message)
      .code(ErrorCode.INVALID_QUERY_ENUM_VALUE.getCode())
      .type(ErrorCode.INVALID_QUERY_ENUM_VALUE.getErrorType());
    Parameter parameter = new Parameter().key(e.getName()).value(String.valueOf(e.getValue()));
    code.addParametersItem(parameter);
    return code;
  }
}
