package org.folio.rspec.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationError {

  String path;
  SeverityType severity;
  DefinitionType definitionType;
  UUID definitionId;
  String message;
  String ruleCode;
}
