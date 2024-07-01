package org.folio.rspec.domain.entity.metadata;

import lombok.Getter;

@Getter
public abstract class SpecMetadata {

  private final Boolean defaultValue;

  protected SpecMetadata(Boolean defaultValue) {
    this.defaultValue = defaultValue;
  }
}
