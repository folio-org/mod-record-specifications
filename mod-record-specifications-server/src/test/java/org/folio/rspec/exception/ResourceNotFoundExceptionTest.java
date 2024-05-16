package org.folio.rspec.exception;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceNotFoundExceptionTest {

  @Test
  void resourceNotFoundForSpecification() {
    Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
      throw ResourceNotFoundException.forSpecification(1L);
    });

    String expectedMessage = "specification with ID [1] was not found";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void resourceNotFoundForSpecificationRule() {
    Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
      throw ResourceNotFoundException.forSpecificationRule(1L);
    });

    String expectedMessage = "specification rule with ID [1] was not found";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
}
