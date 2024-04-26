package org.folio.rspec.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.controller.handler.ServiceExceptionHandler;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

  private final List<ServiceExceptionHandler> exceptionHandlers;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorCollection> globalExceptionHandler(Exception e) {
    for (ServiceExceptionHandler exceptionHandler : exceptionHandlers) {
      if (exceptionHandler.canHandle(e)) {
        return exceptionHandler.handleException(e);
      }
    }
    return ServiceExceptionHandler.fallback(e);
  }

}
