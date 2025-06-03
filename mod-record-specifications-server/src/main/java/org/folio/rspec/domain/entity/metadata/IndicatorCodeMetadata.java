package org.folio.rspec.domain.entity.metadata;

import java.util.UUID;

public record IndicatorCodeMetadata(
  String id,
  String code,
  String scope,
  Boolean defaultValue,
  Boolean deprecated,
  String label
) {

  public IndicatorCodeMetadata(String code, String scope) {
    this(UUID.randomUUID().toString(), code, scope, false, null, null);
  }
}
