package org.folio.rspec.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubfieldUpdateRequestEvent extends UpdateRequestEvent {

  private String targetFieldTag;
  private SubfieldDto subfield;
}
