package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.SCOPE_MODIFICATION_NOT_ALLOWED;

import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScopeModificationNotAllowedExceptionHandler implements ServiceExceptionHandler {

  private static final String PARAMETER_MSG_ARG = "parameter";
  private static final String SCOPE_MSG_ARG = "scope";

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ScopeModificationNotAllowedException) e;
    var messageKey = SCOPE_MODIFICATION_NOT_ALLOWED.getMessageKey()
      .formatted(exception.getModificationType().name().toLowerCase());
    var errorMessage = translationService.format(messageKey,
      PARAMETER_MSG_ARG, exception.getFieldName(), SCOPE_MSG_ARG, exception.getScope().getValue());
    var error = ServiceExceptionHandler.fromErrorCode(SCOPE_MODIFICATION_NOT_ALLOWED)
      .message(errorMessage);
    return ResponseEntity.badRequest().body(new ErrorCollection().addErrorsItem(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ScopeModificationNotAllowedException;
  }
}
