package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_FIELD_INDICATOR;
import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_FIELD_TAG;
import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_INDICATOR_CODE;
import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_SUBFIELD;
import static org.folio.rspec.domain.dto.ErrorCode.UNEXPECTED;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.service.i18n.ExtendedTranslationService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HibernateConstraintViolationExceptionHandler implements ServiceExceptionHandler {

  private static final Map<String, ErrorCode> DB_CONSTRAINTS_MAP = Map.of(
    Field.TAG_UNIQUE_CONSTRAINT, DUPLICATE_FIELD_TAG,
    Indicator.ORDER_UNIQUE_CONSTRAINT, DUPLICATE_FIELD_INDICATOR,
    IndicatorCode.CODE_UNIQUE_CONSTRAINT, DUPLICATE_INDICATOR_CODE,
    Subfield.SUBFIELD_CODE_UNIQUE_CONSTRAINT, DUPLICATE_SUBFIELD
  );

  private final ExtendedTranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var errorCode = getErrorCode(e);
    var errorMessage = errorCode == UNEXPECTED
                       ? translationService.formatUnexpected(e.getMessage())
                       : translationService.format(errorCode.getMessageKey());

    var error = fromErrorCode(errorCode).message(errorMessage);

    var errorCollection = new ErrorCollection().addErrorsItem(error);
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof DataIntegrityViolationException && e.getCause() instanceof ConstraintViolationException;
  }

  private ErrorCode getErrorCode(Exception e) {
    var exception = (ConstraintViolationException) e.getCause();
    var constraintName = exception.getConstraintName();
    return DB_CONSTRAINTS_MAP.getOrDefault(constraintName, UNEXPECTED);
  }
}
