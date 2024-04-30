package org.folio.rspec.controller.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fallback;
import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals(ErrorCode.INVALID_QUERY_VALUE.getErrorType(), error.getType());
  }
}
