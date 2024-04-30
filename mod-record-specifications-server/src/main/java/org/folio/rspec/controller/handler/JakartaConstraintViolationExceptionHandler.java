package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_QUERY_VALUE;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class JakartaConstraintViolationExceptionHandler implements ServiceExceptionHandler {

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ConstraintViolationException) e;
    var errorCollection = buildErrorCollection(exception);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ConstraintViolationException;
  }

  private ErrorCollection buildErrorCollection(ConstraintViolationException exception) {
    var errorCollection = new ErrorCollection();
    exception.getConstraintViolations()
      .forEach(violation -> processViolation(violation, errorCollection));
    return errorCollection;
  }

  private void processViolation(ConstraintViolation<?> violation, ErrorCollection errorCollection) {
    var propertyPath = violation.getPropertyPath();
    for (var node : propertyPath) {
      if (node.getKind() == ElementKind.PARAMETER) {
        var parameter = new Parameter()
          .key(node.getName())
          .value(String.valueOf(violation.getInvalidValue()));
        var error = fromErrorCode(INVALID_QUERY_VALUE).message(violation.getMessage());
        error.addParametersItem(parameter);

        errorCollection.addErrorsItem(error);
      }
    }
  }
}
