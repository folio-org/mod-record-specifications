package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.CONTROL_FIELD_RESOURCE_NOT_ALLOWED;

import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.exception.ResourceValidationFailedException;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceValidationFailedExceptionHandler implements ServiceExceptionHandler {

  private static final String PARAMETER_MSG_ARG = "parameter";

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ResourceValidationFailedException) e;
    var messageKey = CONTROL_FIELD_RESOURCE_NOT_ALLOWED.getMessageKey();
    var errorMessage = translationService.format(messageKey, PARAMETER_MSG_ARG, exception.getResource());
    var error = ServiceExceptionHandler.fromErrorCode(CONTROL_FIELD_RESOURCE_NOT_ALLOWED)
      .message(errorMessage);
    return ResponseEntity.badRequest().body(new ErrorCollection().addErrorsItem(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ResourceValidationFailedException;
  }
}
