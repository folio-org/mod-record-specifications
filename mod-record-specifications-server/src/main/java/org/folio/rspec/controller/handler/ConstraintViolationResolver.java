package org.folio.rspec.controller.handler;

import static org.folio.rspec.controller.handler.ServiceExceptionHandler.fromErrorCode;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.Parameter;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.stereotype.Component;
import org.springframework.validation.MessageCodesResolver;

@Component
@RequiredArgsConstructor
public class ConstraintViolationResolver {

  private final MessageCodesResolver messageCodesResolver;
  private final TranslationService translationService;

  public Collection<Error> processViolation(ConstraintViolation<?> violation, ErrorCode errorCode) {
    var errors = new ArrayList<Error>();
    var descriptor = violation.getConstraintDescriptor();
    var simpleAnnotationName = getSimpleAnnotationName(descriptor);
    var propertyPath = violation.getPropertyPath();
    for (var node : propertyPath) {
      var attributes = new HashMap<>(descriptor.getAttributes());
      var parameter = buildParameter(node, violation);
      var objectName = violation.getRootBeanClass().getSimpleName();
      var codes = messageCodesResolver.resolveMessageCodes(simpleAnnotationName, objectName, node.getName(), null);
      attributes.put("parameter", node.getName());
      var attributesArray = attributes.entrySet().stream()
        .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
        .toArray();
      var message = translationService.format(codes, attributesArray);
      var error = fromErrorCode(errorCode).message(message);
      error.addParametersItem(parameter);
      errors.add(error);
    }
    return errors;
  }

  private String getSimpleAnnotationName(ConstraintDescriptor<?> descriptor) {
    return descriptor.getAnnotation().annotationType().getSimpleName();
  }

  private Parameter buildParameter(Path.Node node, ConstraintViolation<?> violation) {
    return new Parameter()
      .key(node.getName())
      .value(String.valueOf(violation.getInvalidValue()));
  }

}
