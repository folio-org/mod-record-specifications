package org.folio.rspec.domain.entity.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record IndicatorMetadata(
  String id,
  int order,
  Boolean defaultValue,
  String label,
  Map<String, IndicatorCodeMetadata> codes
) {

  public IndicatorMetadata(String order) {
    this(UUID.randomUUID().toString(), Integer.parseInt(order), false, null, new HashMap<>());
  }
}
