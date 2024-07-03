package org.folio.rspec.domain.entity.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record FieldMetadata(
  String id,
  String tag,
  String scope,
  Boolean defaultValue,
  String label,
  Boolean repeatable,
  Boolean required,
  Boolean deprecated,
  String url,
  Map<String, SubfieldMetadata> subfields,
  Map<String, IndicatorMetadata> indicators
) {

  public FieldMetadata(String tag, String scope) {
    this(UUID.randomUUID().toString(), tag, scope, false, null, null, null,
      null, null, new HashMap<>(), new HashMap<>());
  }

}
