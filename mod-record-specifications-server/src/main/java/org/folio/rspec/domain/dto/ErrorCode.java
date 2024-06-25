package org.folio.rspec.domain.dto;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INVALID_QUERY_VALUE("invalid-query-value", "101", null),
  INVALID_QUERY_ENUM_VALUE("invalid-query-enum-value", "102", "invalid.request.query-param.enum"),
  INVALID_REQUEST_PARAMETER("invalid-request-parameter", "103", null),
  DUPLICATE_FIELD_TAG("duplicate-specification-field-tag", "104", "specification.field.tag.duplicate"),
  DUPLICATE_FIELD_LABEL("duplicate-specification-field-label", "104", "specification.field.label.duplicate"),
  SPECIFICATION_FETCH_FAILED("specification-fetch-failed", "105", "specification.fetch.failed"),
  RESOURCE_NOT_FOUND("resource-not-found", "404", "specification.resource.not-found"),
  UNEXPECTED("unexpected", "500", "unexpected"),

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
