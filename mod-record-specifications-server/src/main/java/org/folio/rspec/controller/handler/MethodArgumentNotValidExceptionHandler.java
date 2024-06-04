package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_REQUEST_PARAMETER;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Log4j2
@Component
@RequiredArgsConstructor
public class MethodArgumentNotValidExceptionHandler implements ServiceExceptionHandler {

  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (MethodArgumentNotValidException) e;
    var errorCollection = new ErrorCollection();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      var error = fromErrorCode(INVALID_REQUEST_PARAMETER);
      String translatedMessage = null;
      for (String code : fieldError.getCodes() == null ? new String[0] : fieldError.getCodes()) {
        var translation = translationService.format(code, "field", fieldError.getField());
        if (!translation.endsWith(code)) {
          translatedMessage = translation;
          break;
        }
      }
      error.setMessage(translatedMessage == null ? fieldError.getDefaultMessage() : translatedMessage);
      var parameter = new Parameter().key(fieldError.getField()).value(String.valueOf(fieldError.getRejectedValue()));
      error.addParametersItem(parameter);
      errorCollection.addErrorsItem(error);
    }
    return ResponseEntity.badRequest().body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentNotValidException;
  }
}
