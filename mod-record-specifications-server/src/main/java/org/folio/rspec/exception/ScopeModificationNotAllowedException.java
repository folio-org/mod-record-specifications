package org.folio.rspec.exception;

import lombok.Getter;
import org.folio.rspec.domain.dto.Scope;

@Getter
public final class ScopeModificationNotAllowedException extends RuntimeException {

  private final Scope scope;
  private final ModificationType modificationType;
  private final String fieldName;

  private ScopeModificationNotAllowedException(Scope scope, ModificationType modificationType, String fieldName) {
    super("%s action is not allowed for scope %s".formatted(modificationType, scope));
    this.scope = scope;
    this.modificationType = modificationType;
    this.fieldName = fieldName;
  }

  public static ScopeModificationNotAllowedException forUpdate(Scope scope, String fieldName) {
    return new ScopeModificationNotAllowedException(scope, ModificationType.UPDATE, fieldName);
  }

  public static ScopeModificationNotAllowedException forDelete(Scope scope, String fieldName) {
    return new ScopeModificationNotAllowedException(scope, ModificationType.DELETE, fieldName);
  }

  public enum ModificationType {
    CREATE, UPDATE, DELETE
  }
}
