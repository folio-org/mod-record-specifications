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
    var exception = (MethodArgumentTypeMismatchException) e;
    var requiredType = exception.getRequiredType();
    var errorCollection = new ErrorCollection();

    if (requiredType != null && requiredType.isEnum()) {
      var message = buildErrorMessage(exception, requiredType);
      var code = buildErrorCode(exception, message);
      errorCollection.addErrorsItem(code);
    }

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentTypeMismatchException;
  }

  private String buildErrorMessage(MethodArgumentTypeMismatchException e, Class<?> requiredType) {
    var enumConstants = requiredType.getEnumConstants();
    var rootCause = e.getRootCause();
    return buildErrorMessage(rootCause, enumConstants);
  }

  private String buildErrorMessage(Throwable rootCause, Object[] enumConstants) {
    return "%sPossible values: %s".formatted(
      rootCause != null ? rootCause.getMessage() + ". " : "",
      Arrays.toString(enumConstants)
    );
  }

  private Error buildErrorCode(MethodArgumentTypeMismatchException e, String message) {
    var code = new Error()
      .message(message)
      .code(ErrorCode.INVALID_QUERY_ENUM_VALUE.getCode())
      .type(ErrorCode.INVALID_QUERY_ENUM_VALUE.getErrorType());
    var parameter = new Parameter().key(e.getName()).value(String.valueOf(e.getValue()));
    code.addParametersItem(parameter);
    return code;
  }
}
