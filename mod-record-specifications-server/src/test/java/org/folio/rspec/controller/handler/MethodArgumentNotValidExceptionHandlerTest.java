package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.INVALID_REQUEST_PARAMETER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.spring.i18n.service.TranslationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MethodArgumentNotValidExceptionHandlerTest {

  @Mock
  private TranslationService translationService;

  @Mock
  private MethodArgumentNotValidException methodArgumentNotValidException;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private MethodArgumentNotValidExceptionHandler exceptionHandler;

  @Test
  void handleException_ReturnsBadRequestWithErrorCollection() {
    // Arrange
    var field = "testField";
    var defaultMessage = "must not be null";
    var rejectedValue = "null";
    var codes = new String[] {"c1", "c2"};
    var fieldError = new FieldError("objectName", field, rejectedValue, false, codes, null, defaultMessage);
    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
    when(translationService.format(codes, "field", field, "boundary", "undefined")).thenReturn(defaultMessage);

    // Act
    var responseEntity = exceptionHandler.handleException(methodArgumentNotValidException);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    var errorCollection = responseEntity.getBody();
    assertNotNull(errorCollection);
    assertNotNull(errorCollection.getErrors());
    assertEquals(1, errorCollection.getErrors().size());

    var error = errorCollection.getErrors().get(0);
    assertEquals(defaultMessage, error.getMessage());
    assertEquals(INVALID_REQUEST_PARAMETER.getCode(), error.getCode());
    assertEquals(INVALID_REQUEST_PARAMETER.getType(), error.getType());

    var parameter = error.getParameters().get(0);
    assertEquals(field, parameter.getKey());
    assertEquals(rejectedValue, parameter.getValue());
  }

  @Test
  void canHandle_ReturnsTrueForMethodArgumentNotValidException() {
    // Act
    boolean canHandle = exceptionHandler.canHandle(methodArgumentNotValidException);

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
