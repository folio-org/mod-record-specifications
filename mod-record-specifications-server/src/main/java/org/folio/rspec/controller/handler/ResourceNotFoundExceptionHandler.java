package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.errorCollection;
import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.RESOURCE_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceNotFoundExceptionHandler implements ServiceExceptionHandler {

  private static final String RESOURCE_NAME_MSG_ARG = "resourceName";
  private static final String RESOURCE_ID_MSG_ARG = "resourceId";

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ResourceNotFoundException) e;
    var message = translationService.format(RESOURCE_NOT_FOUND.getMessageKey(),
      RESOURCE_NAME_MSG_ARG, exception.getResource().getName(), RESOURCE_ID_MSG_ARG, exception.getId());
    var error = fromErrorCode(RESOURCE_NOT_FOUND).message(message);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorCollection(error));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ResourceNotFoundException;
  }
}
