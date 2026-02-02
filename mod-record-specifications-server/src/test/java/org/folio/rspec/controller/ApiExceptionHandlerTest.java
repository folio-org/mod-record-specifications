package org.folio.rspec.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.rspec.controller.handler.ServiceExceptionHandler;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

  @Mock
  private ServiceExceptionHandler serviceExceptionHandler;

  private ApiExceptionHandler apiExceptionHandler;

  @BeforeEach
  void setUp() {
    List<ServiceExceptionHandler> exceptionHandlers = List.of(serviceExceptionHandler);
    apiExceptionHandler = new ApiExceptionHandler(exceptionHandlers);
  }

  @Test
  void globalExceptionHandler_canHandle() {
    Exception exception = new IllegalArgumentException("Test exception");
    ErrorCollection errorCollection = new ErrorCollection();
    ResponseEntity<ErrorCollection> expectedResponse = ResponseEntity.unprocessableContent().body(errorCollection);

    when(serviceExceptionHandler.canHandle(exception)).thenReturn(true);
    when(serviceExceptionHandler.handleException(exception)).thenReturn(expectedResponse);

    ResponseEntity<ErrorCollection> result = apiExceptionHandler.globalExceptionHandler(exception);

    assertEquals(expectedResponse.getStatusCode(), result.getStatusCode());
    verify(serviceExceptionHandler).handleException(exception);
  }

  @Test
  void globalExceptionHandler_cannotHandle() {
    Exception exception = new IllegalArgumentException("Test exception");
    ErrorCollection errorCollection = new ErrorCollection();
    ResponseEntity<ErrorCollection> expectedResponse = ResponseEntity.internalServerError().body(errorCollection);

    when(serviceExceptionHandler.canHandle(exception)).thenReturn(false);

    ResponseEntity<ErrorCollection> result = apiExceptionHandler.globalExceptionHandler(exception);

    assertEquals(expectedResponse.getStatusCode(), result.getStatusCode());
    verify(serviceExceptionHandler, never()).handleException(exception);
  }
}
