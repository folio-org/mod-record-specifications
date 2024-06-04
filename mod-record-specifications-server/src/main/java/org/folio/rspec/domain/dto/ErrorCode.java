package org.folio.rspec.domain.dto;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INVALID_QUERY_VALUE("invalid-query-value", "101", null),
  INVALID_QUERY_ENUM_VALUE("invalid-query-enum-value", "102", null),
  INVALID_REQUEST_PARAMETER("invalid-request-parameter", "103", null),
  DUPLICATE_SPECIFICATION_FIELD("duplicate-specification-field", "104", "specification.field.tag.duplicate"),
  RESOURCE_NOT_FOUND("resource-not-found", "404", "specification.resource.not-found"),
  UNEXPECTED("unexpected", "500", null),

  ;

  private final String type;
  private final String code;
  private final String messageKey;

  ErrorCode(String type, String code, String messageKey) {
    this.type = type;
    this.code = code;
    this.messageKey = messageKey;
  }
}
