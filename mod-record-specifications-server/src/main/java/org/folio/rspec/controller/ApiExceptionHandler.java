package org.folio.rspec.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.controller.handler.ServiceExceptionHandler;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

  private final List<ServiceExceptionHandler> exceptionHandlers;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorCollection> globalExceptionHandler(Exception e) {
    log.warn("Trying to handle [exception: {}}, message: {}]", e.getClass().getName(), e.getMessage());
    for (ServiceExceptionHandler exceptionHandler : exceptionHandlers) {
      if (exceptionHandler.canHandle(e)) {
        return exceptionHandler.handleException(e);
      }
    }
    log.error("Failed to handle exception", e);
    return ServiceExceptionHandler.fallback(e);
  }

}
