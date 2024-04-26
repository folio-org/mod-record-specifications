package org.folio.rspec.controller.handler;

import static jakarta.validation.ElementKind.PARAMETER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.folio.rspec.domain.dto.Error;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@UnitTest
class JakartaConstraintViolationExceptionHandlerTest {

  private final JakartaConstraintViolationExceptionHandler handler = new JakartaConstraintViolationExceptionHandler();

  @Test
  void testCanHandle() {
    assertTrue(handler.canHandle(new ConstraintViolationException(new HashSet<>())));
    assertFalse(handler.canHandle(new Exception()));
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void testHandleException() {
    final var errorMessage = "Test error message";
    final var parameter = "test";
    final var parameterValue = "TestInvalidValue";

    final Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Path path = mock(Path.class);
    Path.Node node = mock(Path.Node.class);
    Iterator<Path.Node> iterator = mock(Iterator.class);
    when(violation.getMessage()).thenReturn(errorMessage);
    when(violation.getInvalidValue()).thenReturn(parameterValue);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(path.iterator()).thenReturn(iterator);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(node);
    when(node.getKind()).thenReturn(PARAMETER);
    when(node.getName()).thenReturn(parameter);

    violations.add(violation);
    var exception = new ConstraintViolationException("Test message", violations);

    var response = handler.handleException(exception);

    assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getErrors()).isNotNull().hasSize(1);
    assertThat(response.getBody().getErrors().get(0))
      .extracting(Error::getMessage, Error::getType, Error::getCode, error -> error.getParameters().get(0).getKey(),
        error -> error.getParameters().get(0).getValue())
      .containsExactly(errorMessage, "invalid-query-value", "101", parameter, parameterValue);
  }
}
