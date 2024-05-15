package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.RESOURCE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UnitTest
class ResourceNotFoundExceptionHandlerTest {

  private final ResourceNotFoundExceptionHandler exceptionHandler = new ResourceNotFoundExceptionHandler();

  @Test
  void handleException_ReturnsNotFoundWithErrorCollection() {
    // Arrange
    var resourceId = "resource-id";
    var exception = ResourceNotFoundException.forSpecification(resourceId);

    // Act
    ResponseEntity<ErrorCollection> responseEntity = exceptionHandler.handleException(exception);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    var errorCollection = responseEntity.getBody();
    assertNotNull(errorCollection);
    assertNotNull(errorCollection.getErrors());
    assertEquals(1, errorCollection.getErrors().size());

    var error = errorCollection.getErrors().get(0);
    assertEquals(RESOURCE_NOT_FOUND.getCode(), error.getCode());
    assertEquals(RESOURCE_NOT_FOUND.getErrorType(), error.getType());
    assertEquals("specification with ID [%s] was not found".formatted(resourceId), error.getMessage());
  }

  @Test
  void canHandle_ReturnsTrueForResourceNotFoundException() {
    // Act
    boolean canHandle = exceptionHandler.canHandle(ResourceNotFoundException.forSpecificationRule(1));

    // Assert
    assertTrue(canHandle);
  }

  @Test
  void canHandle_ReturnsFalseForOtherExceptions() {
    // Act
    boolean canHandle = exceptionHandler.canHandle(new Exception());

    // Assert
    assertFalse(canHandle);
  }
}
