package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_SPECIFICATION_FIELD;
import static org.folio.rspec.domain.dto.ErrorCode.UNEXPECTED;

import java.util.Map;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class HibernateConstraintViolationExceptionHandler implements ServiceExceptionHandler {

  private static final Map<String, ErrorCode> DB_CONSTRAINTS_MAP = Map.of(
    "uc_field_tag_specification_id", DUPLICATE_SPECIFICATION_FIELD
  );

  private static final Map<ErrorCode, String> ERROR_MESSAGE_MAP = Map.of(
    DUPLICATE_SPECIFICATION_FIELD, "Can only have one validation rule per MARC field/tag number.",
    UNEXPECTED, "Unexpected constraint violation."
  );

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ConstraintViolationException) e.getCause();
    var constraintName = exception.getConstraintName();
    var errorCode = DB_CONSTRAINTS_MAP.getOrDefault(constraintName, UNEXPECTED);
    var error = ServiceExceptionHandler.fromErrorCode(errorCode)
      .message(ERROR_MESSAGE_MAP.get(errorCode));
    var errorCollection = new ErrorCollection().addErrorsItem(error);
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof DataIntegrityViolationException && e.getCause() instanceof ConstraintViolationException;
  }
}
