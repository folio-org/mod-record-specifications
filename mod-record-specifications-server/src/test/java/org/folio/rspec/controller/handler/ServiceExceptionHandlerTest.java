package org.folio.rspec.controller.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fallback;
import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UnitTest
class ServiceExceptionHandlerTest {

  @Test
  void testFallback() {
    Exception exception = new Exception("Test message");
    ResponseEntity<ErrorCollection> responseEntity = fallback(exception);

    assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getErrors()).isNotNull().hasSize(1);
    assertThat(responseEntity.getBody().getErrors().get(0).getMessage()).isEqualTo("Unexpected error: Test message");
  }

  @Test
  void testFromErrorCode() {
    Error error = fromErrorCode(ErrorCode.INVALID_QUERY_VALUE);

    assertEquals(ErrorCode.INVALID_QUERY_VALUE.getCode(), error.getCode());
    assertEquals(ErrorCode.INVALID_QUERY_VALUE.getType(), error.getType());
  }

  @Test
  void errorCollection_ReturnsEmptyCollectionWhenNoErrorsProvided() {
    // Act
    ErrorCollection result = ServiceExceptionHandler.errorCollection();

    // Assert
    assertTrue(result.getErrors().isEmpty());
  }

  @Test
  void errorCollection_ReturnsCollectionWithSingleError() {
    // Arrange
    Error error = new Error().message("Error message");

    // Act
    ErrorCollection result = ServiceExceptionHandler.errorCollection(error);

    // Assert
    assertEquals(1, result.getErrors().size());
    assertEquals("Error message", result.getErrors().get(0).getMessage());
  }

  @Test
  void errorCollection_ReturnsCollectionWithMultipleErrors() {
    // Arrange
    Error error1 = new Error().message("Error message 1");
    Error error2 = new Error().message("Error message 2");

    // Act
    ErrorCollection result = ServiceExceptionHandler.errorCollection(error1, error2);

    // Assert
    assertEquals(2, result.getErrors().size());
    assertEquals("Error message 1", result.getErrors().get(0).getMessage());
    assertEquals("Error message 2", result.getErrors().get(1).getMessage());
  }
}
