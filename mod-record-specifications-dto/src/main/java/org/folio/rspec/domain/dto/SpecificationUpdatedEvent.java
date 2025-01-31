package org.folio.rspec.domain.dto;

import java.util.UUID;

public record SpecificationUpdatedEvent(UUID specificationId,
                                        String tenantId,
                                        Family family,
                                        FamilyProfile profile,
                                        UpdateExtent updateExtent) {

  public enum UpdateExtent {
    FULL, PARTIAL
  }
}
