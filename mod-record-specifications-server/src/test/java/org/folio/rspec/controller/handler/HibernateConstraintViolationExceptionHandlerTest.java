package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_FIELD_INDICATOR;
import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_INDICATOR_CODE;
import static org.folio.rspec.domain.dto.ErrorCode.DUPLICATE_SPECIFICATION_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.service.i18n.ExtendedTranslationService;
import org.folio.spring.testing.type.UnitTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HibernateConstraintViolationExceptionHandlerTest {

  @Mock
  private ExtendedTranslationService translationService;

  @InjectMocks
  private HibernateConstraintViolationExceptionHandler handler;

  @MethodSource("uniqueConstraintTestSource")
  @ParameterizedTest
  void handleException_duplicateField(String constraintName, ErrorCode errorCode, String expectedMessage) {
    var cause = mock(ConstraintViolationException.class);
    when(cause.getConstraintName()).thenReturn(constraintName);
    var exception = new DataIntegrityViolationException("Error", cause);
    when(translationService.format(errorCode.getMessageKey())).thenReturn(expectedMessage);

    var response = handler.handleException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    var errorCollection = response.getBody();
    assertNotNull(errorCollection);
    assertEquals(1, errorCollection.getErrors().size());
    assertEquals(expectedMessage, errorCollection.getErrors().get(0).getMessage());
  }

  @Test
  void handleException_unexpectedError() {
    var cause = mock(ConstraintViolationException.class);
    when(cause.getConstraintName()).thenReturn("unknown_constraint");
    var expectedMessage = "Unexpected error";
    var exception = new DataIntegrityViolationException("Error", cause);
    when(translationService.formatUnexpected(anyString())).thenReturn(expectedMessage);

    var response = handler.handleException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    var errorCollection = response.getBody();
    assertNotNull(errorCollection);
    assertEquals(1, errorCollection.getErrors().size());
    assertEquals(expectedMessage, errorCollection.getErrors().get(0).getMessage());
  }

  @Test
  void canHandle_validException() {
    var cause = mock(ConstraintViolationException.class);
    var exception = new DataIntegrityViolationException("Error", cause);

    boolean canHandle = handler.canHandle(exception);

    assertTrue(canHandle);
  }

  @Test
  void canHandle_invalidException() {
    var exception = new Exception("Error");

    boolean canHandle = handler.canHandle(exception);

    assertFalse(canHandle);
  }

  public static Stream<Arguments> uniqueConstraintTestSource() {
    return Stream.of(
      arguments("uc_field_tag_specification_id", DUPLICATE_SPECIFICATION_FIELD,
        "Duplicate specification field error message"),
      arguments("uc_indicator_order_field_id", DUPLICATE_FIELD_INDICATOR, "Duplicate field indicator error message"),
      arguments("uc_indicator_code_indicator_id", DUPLICATE_INDICATOR_CODE, "Duplicate indicator code error message")
    );
  }
}
