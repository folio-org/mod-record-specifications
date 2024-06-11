package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.RESOURCE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.i18n.service.TranslationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceNotFoundExceptionHandlerTest {

  @Mock
  private TranslationService translationService;

  @InjectMocks
  private ResourceNotFoundExceptionHandler exceptionHandler;

  @Test
  void handleException_ReturnsNotFoundWithErrorCollection() {
    // Arrange
    var resourceId = "resource-id";
    var expectedMessage = "specification with ID [%s] was not found".formatted(resourceId);
    var exception = ResourceNotFoundException.forSpecification(resourceId);
    when(translationService.format(RESOURCE_NOT_FOUND.getMessageKey(), "resourceName",
      ResourceNotFoundException.Resource.SPECIFICATION.getName(), "resourceId", resourceId))
      .thenReturn(expectedMessage);

    // Act
    var responseEntity = exceptionHandler.handleException(exception);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    var errorCollection = responseEntity.getBody();
    assertNotNull(errorCollection);
    assertNotNull(errorCollection.getErrors());
    assertEquals(1, errorCollection.getErrors().size());

    var error = errorCollection.getErrors().get(0);
    assertEquals(RESOURCE_NOT_FOUND.getCode(), error.getCode());
    assertEquals(RESOURCE_NOT_FOUND.getType(), error.getType());
    assertEquals(expectedMessage, error.getMessage());
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
