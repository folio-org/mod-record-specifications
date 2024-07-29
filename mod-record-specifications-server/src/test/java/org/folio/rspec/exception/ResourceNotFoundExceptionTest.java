package org.folio.rspec.exception;

import static org.folio.rspec.exception.ResourceNotFoundException.forField;
import static org.folio.rspec.exception.ResourceNotFoundException.forIndicator;
import static org.folio.rspec.exception.ResourceNotFoundException.forIndicatorCode;
import static org.folio.rspec.exception.ResourceNotFoundException.forSpecification;
import static org.folio.rspec.exception.ResourceNotFoundException.forSpecificationRule;
import static org.folio.rspec.exception.ResourceNotFoundException.forSubfield;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.folio.rspec.exception.ResourceNotFoundException.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ResourceNotFoundExceptionTest {

  @MethodSource("testSource")
  @ParameterizedTest
  void resourceNotFoundForField(Supplier<ResourceNotFoundException> supplier, Resource resource) {
    Exception exception = supplier.get();

    String expectedMessage = "%s with ID [1] was not found".formatted(resource.getName());
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  public static Stream<Arguments> testSource() {
    return Stream.of(
      arguments((Supplier<ResourceNotFoundException>) () -> forSpecificationRule(1L), Resource.SPECIFICATION_RULE),
      arguments((Supplier<ResourceNotFoundException>) () -> forSpecification(1L), Resource.SPECIFICATION),
      arguments((Supplier<ResourceNotFoundException>) () -> forField(1L), Resource.FIELD_DEFINITION),
      arguments((Supplier<ResourceNotFoundException>) () -> forIndicator(1L), Resource.FIELD_INDICATOR),
      arguments((Supplier<ResourceNotFoundException>) () -> forIndicatorCode(1L), Resource.INDICATOR_CODE),
      arguments((Supplier<ResourceNotFoundException>) () -> forSubfield(1L), Resource.SUBFIELD)
    );
  }
}
