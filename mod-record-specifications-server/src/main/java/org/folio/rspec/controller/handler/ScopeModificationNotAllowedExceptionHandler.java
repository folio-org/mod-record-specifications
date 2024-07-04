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

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ScopeModificationNotAllowedException) e;
    var error = ServiceExceptionHandler.fromErrorCode(SCOPE_MODIFICATION_NOT_ALLOWED)
      .message(translationService.format(SCOPE_MODIFICATION_NOT_ALLOWED.getMessageKey(),
        "parameter", exception.getFieldName(), "scope", exception.getScope().getValue()));
    return ResponseEntity.badRequest().body(new ErrorCollection().addErrorsItem(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ScopeModificationNotAllowedException;
  }
}
