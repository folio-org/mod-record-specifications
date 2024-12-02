package org.folio.rspec.domain.dto;

import java.util.UUID;

public record SpecificationUpdatedEvent(UUID specificationId,
                                        String tenantId) {

  public enum UpdateExtent {
    FULL, PARTIAL
  }
}
