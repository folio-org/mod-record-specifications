package org.folio.rspec.domain.dto;

import lombok.Getter;

@Getter
public enum DefinitionType {

  FIELD("field"),
  INDICATOR("indicator"),
  INDICATOR_CODE("indicator-code"),
  SUBFIELD("subfield");

  private final String type;

  DefinitionType(String type) {
    this.type = type;
  }
}
