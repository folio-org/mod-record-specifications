package org.folio.rspec.domain.dto;

import lombok.Getter;

@Getter
public enum SeverityType {

  WARN("warn"),
  ERROR("error");

  private final String type;

  SeverityType(String type) {
    this.type = type;
  }
}
