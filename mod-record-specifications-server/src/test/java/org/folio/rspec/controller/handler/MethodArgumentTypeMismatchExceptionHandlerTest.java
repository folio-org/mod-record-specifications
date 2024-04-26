package org.folio.rspec.controller.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@UnitTest
class MethodArgumentTypeMismatchExceptionHandlerTest {

  private final MethodArgumentTypeMismatchExceptionHandler handler = new MethodArgumentTypeMismatchExceptionHandler();

  @Test
  void testCanHandle() {
    assertTrue(handler.canHandle(new MethodArgumentTypeMismatchException("123", null, null, null, null)));
    assertFalse(handler.canHandle(new Exception()));
  }

  @Test
  void testHandleException() throws NoSuchMethodException {
    Method method = MethodArgumentTypeMismatchExceptionHandler.class.getMethod("handleException", Exception.class);
    MethodParameter methodParameter = new MethodParameter(method, -1);
    MethodArgumentTypeMismatchException exception =
      new MethodArgumentTypeMismatchException("invalidValue", TestEnum.class, "arg1", methodParameter, null);

    ResponseEntity<ErrorCollection> response = handler.handleException(exception);

    assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getErrors()).isNotNull().hasSize(1);
    assertThat(response.getBody().getErrors().get(0))
      .extracting(Error::getMessage, Error::getType, Error::getCode, error -> error.getParameters().get(0).getKey(),
        error -> error.getParameters().get(0).getValue())
      .containsExactly("Possible values: [V1, V2]", "invalid-query-enum-value", "102", "arg1", "invalidValue");
  }

  private enum TestEnum {
    V1, V2;
  }
}
