package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_QUERY_VALUE;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.MessageCodesResolver;

@Component
@RequiredArgsConstructor
public class JakartaConstraintViolationExceptionHandler implements ServiceExceptionHandler {

  private final MessageCodesResolver messageCodesResolver;
  private final TranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (ConstraintViolationException) e;
    var errorCollection = buildErrorCollection(exception);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorCollection);
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof ConstraintViolationException;
  }

  private ErrorCollection buildErrorCollection(ConstraintViolationException exception) {
    var errorCollection = new ErrorCollection();
    exception.getConstraintViolations()
      .forEach(violation -> processViolation(violation, errorCollection));
    return errorCollection;
  }

  private void processViolation(ConstraintViolation<?> violation, ErrorCollection errorCollection) {
    var descriptor = violation.getConstraintDescriptor();
    var simpleAnnotationName = getSimpleAnnotationName(descriptor);
    var propertyPath = violation.getPropertyPath();
    for (var node : propertyPath) {
      if (node.getKind() == ElementKind.PARAMETER) {
        var expectedValue = getExpectedValue(descriptor);
        var parameter = buildParameter(node, violation);
        var message = buildMessage(simpleAnnotationName, violation, node, expectedValue);
        var error = fromErrorCode(INVALID_QUERY_VALUE).message(message);
        error.addParametersItem(parameter);
        errorCollection.addErrorsItem(error);
      }
    }
  }

  private String getSimpleAnnotationName(ConstraintDescriptor<?> descriptor) {
    return descriptor.getAnnotation().annotationType().getSimpleName();
  }

  private String getExpectedValue(ConstraintDescriptor<?> descriptor) {
    return descriptor.getAttributes().get("value").toString();
  }

  private Parameter buildParameter(Path.Node node, ConstraintViolation<?> violation) {
    return new Parameter()
      .key(node.getName())
      .value(String.valueOf(violation.getInvalidValue()));
  }

  private String buildMessage(String code, ConstraintViolation<?> violation, Path.Node node, String expectedValue) {
    var objectName = violation.getRootBeanClass().getSimpleName();
    var codes = messageCodesResolver.resolveMessageCodes(code, objectName, node.getName(), null);
    return translationService.format(codes, "parameter", node.getName(), "value", expectedValue);
  }
}
