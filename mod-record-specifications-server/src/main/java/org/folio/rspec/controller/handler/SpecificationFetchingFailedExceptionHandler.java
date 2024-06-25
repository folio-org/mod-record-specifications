package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.SPECIFICATION_FETCH_FAILED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpecificationFetchingFailedExceptionHandler implements ServiceExceptionHandler {

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var error = ServiceExceptionHandler.fromErrorCode(SPECIFICATION_FETCH_FAILED).message(translationService.format(
      SPECIFICATION_FETCH_FAILED.getMessageKey()));
    return ResponseEntity.badRequest().body(new ErrorCollection().errors(List.of(error)));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof SpecificationFetchingFailedException;
  }
}
