package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.INVALID_QUERY_VALUE;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JakartaConstraintViolationExceptionHandler implements ServiceExceptionHandler {

  private final ConstraintViolationResolver constraintViolationResolver;

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
    var errorList = exception.getConstraintViolations().stream()
      .flatMap(violation -> constraintViolationResolver.processViolation(violation, INVALID_QUERY_VALUE).stream())
      .toList();

    return new ErrorCollection().errors(errorList);
  }

}
