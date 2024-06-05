package org.folio.rspec.controller.handler;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.service.i18n.ExtendedTranslationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MethodArgumentTypeMismatchExceptionHandlerTest {

  @Mock
  private ExtendedTranslationService translationService;

  @Mock
  private MethodArgumentTypeMismatchException exception;

  @InjectMocks
  private MethodArgumentTypeMismatchExceptionHandler handler;

  @Test
  void testCanHandle() {
    assertTrue(handler.canHandle(new MethodArgumentTypeMismatchException("123", null, null, null, null)));
    assertFalse(handler.canHandle(new Exception()));
  }

  @Test
  void testHandleException_unexpectedEnumValue() {
    var expectedMessage = "error message";
    var handlerClass = TestEnum.class;

    when(exception.getName()).thenReturn("arg1");
    when(exception.getRequiredType()).thenAnswer(invocation -> handlerClass);
    when(exception.getValue()).thenReturn("invalidValue");
    when(exception.getRootCause()).thenReturn(new RuntimeException("Invalid 'test' value"));
    when(translationService.formatList(anyCollection())).thenReturn("V1 and V2");
    when(translationService.format(anyString(), eq("invalidValue"), eq("test"), eq("possibleValues"), eq("V1 and V2")))
      .thenReturn(expectedMessage);

    var response = handler.handleException(exception);

    assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getErrors()).isNotNull().hasSize(1);
    assertThat(response.getBody().getErrors().get(0))
      .extracting(Error::getMessage, Error::getType, Error::getCode, error -> error.getParameters().get(0).getKey(),
        error -> error.getParameters().get(0).getValue())
      .containsExactly(expectedMessage, "invalid-query-enum-value", "102", "arg1", "invalidValue");
  }

  @Test
  void testHandleException_unexpectedError() {
    var handlerClass = MethodArgumentTypeMismatchExceptionHandler.class;
    var expectedMessage = "error message";
    when(exception.getRequiredType()).thenAnswer(invocation -> handlerClass);
    when(exception.getMessage()).thenReturn(expectedMessage);
    when(translationService.formatUnexpected(expectedMessage)).thenReturn(expectedMessage);

    var response = handler.handleException(exception);

    assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getErrors()).isNotNull().hasSize(1);
    assertThat(response.getBody().getErrors().get(0))
      .extracting(Error::getMessage, Error::getType, Error::getCode, Error::getParameters)
      .containsExactly(expectedMessage, "unexpected", "500", emptyList());
  }

  private enum TestEnum {
    V1, V2;
  }
}
