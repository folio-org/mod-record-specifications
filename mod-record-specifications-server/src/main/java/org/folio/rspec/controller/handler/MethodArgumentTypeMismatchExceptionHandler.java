package org.folio.rspec.controller.handler;

import static org.folio.rspec.domain.dto.ErrorCode.INVALID_QUERY_ENUM_VALUE;
import static org.folio.rspec.domain.dto.ErrorCode.INVALID_QUERY_UUID_VALUE;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Error;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.ErrorCollection;
import org.folio.rspec.domain.dto.Parameter;
import org.folio.rspec.service.i18n.ExtendedTranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Component
@RequiredArgsConstructor
public class MethodArgumentTypeMismatchExceptionHandler implements ServiceExceptionHandler {

  private static final Pattern ENUM_INVALID_VALUE_PATTERN = Pattern.compile("'(.*)'");
  private static final String UNDEFINED_VALUE = "undefined";
  private static final String INVALID_VALUE_MSG_ARG = "invalidValue";
  private static final String POSSIBLE_VALUES_MSG_ARG = "possibleValues";

  private final ExtendedTranslationService translationService;

  @Override
  public ResponseEntity<ErrorCollection> handleException(Exception e) {
    var exception = (MethodArgumentTypeMismatchException) e;
    var requiredType = exception.getRequiredType();

    if (requiredType != null && requiredType.isEnum()) {
      return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, buildEnumError(exception, requiredType));
    }
    if (requiredType != null && requiredType.isAssignableFrom(UUID.class)) {
      return buildResponse(HttpStatus.BAD_REQUEST, buildUuidError(exception));
    }
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, buildUnexpectedError(exception));
  }

  @Override
  public boolean canHandle(Exception e) {
    return e instanceof MethodArgumentTypeMismatchException;
  }

  private ResponseEntity<ErrorCollection> buildResponse(HttpStatus status, Error error) {
    return ResponseEntity.status(status).body(new ErrorCollection().addErrorsItem(error));
  }

  private Error buildEnumError(MethodArgumentTypeMismatchException e, Class<?> requiredType) {
    var message = buildEnumErrorMessage(e, requiredType.getEnumConstants());
    return new Error()
      .message(message)
      .code(INVALID_QUERY_ENUM_VALUE.getCode())
      .type(INVALID_QUERY_ENUM_VALUE.getType())
      .addParametersItem(new Parameter().key(e.getName()).value(String.valueOf(e.getValue())));
  }

  private Error buildUuidError(MethodArgumentTypeMismatchException e) {
    var inputValue = String.valueOf(e.getValue());
    var message = buildUuidErrorMessage(inputValue);
    return new Error()
      .message(message)
      .code(INVALID_QUERY_UUID_VALUE.getCode())
      .type(INVALID_QUERY_UUID_VALUE.getType())
      .addParametersItem(new Parameter().key(e.getName()).value(inputValue));
  }

  private String buildUuidErrorMessage(String inputValue) {
    return translationService.format(INVALID_QUERY_UUID_VALUE.getMessageKey(),
      INVALID_VALUE_MSG_ARG, inputValue);
  }

  private String buildEnumErrorMessage(MethodArgumentTypeMismatchException e, Object[] enumConstants) {
    return translationService.format(INVALID_QUERY_ENUM_VALUE.getMessageKey(),
      INVALID_VALUE_MSG_ARG, getInvalidValue(e.getRootCause()),
      POSSIBLE_VALUES_MSG_ARG, translationService.formatList(Arrays.asList(enumConstants)));
  }

  private String getInvalidValue(Throwable throwable) {
    if (throwable == null) {
      return UNDEFINED_VALUE;
    }
    var matcher = ENUM_INVALID_VALUE_PATTERN.matcher(throwable.getMessage());
    return matcher.find() ? matcher.group(1) : UNDEFINED_VALUE;
  }

  private Error buildUnexpectedError(MethodArgumentTypeMismatchException exception) {
    return ServiceExceptionHandler.fromErrorCode(ErrorCode.UNEXPECTED)
      .message(translationService.formatUnexpected(exception.getMessage()));
  }
}
