package org.folio.rspec.domain.entity.metadata;

import java.util.UUID;

public record SubfieldMetadata(
  String id,
  String code,
  String scope,
  Boolean defaultValue,
  String label,
  Boolean repeatable,
  Boolean required,
  Boolean deprecated,
  String url
) {

  public SubfieldMetadata(String code, String scope) {
    this(UUID.randomUUID().toString(), code, scope, false, null, null, null, null, null);
  }
}
