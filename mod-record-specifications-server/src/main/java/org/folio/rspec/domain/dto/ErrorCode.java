package org.folio.rspec.domain.dto;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INVALID_QUERY_VALUE("invalid-query-value", "101"),
  INVALID_QUERY_ENUM_VALUE("invalid-query-enum-value", "102"),
  UNEXPECTED("unexpected", "500"),

  ;

  private final String errorType;
  private final String code;

  ErrorCode(String errorType, String code) {
    this.errorType = errorType;
    this.code = code;
  }
}
