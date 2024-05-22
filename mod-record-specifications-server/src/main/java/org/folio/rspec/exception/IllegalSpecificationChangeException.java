package org.folio.rspec.exception;

import lombok.Getter;
import org.folio.rspec.domain.dto.Scope;

public class IllegalSpecificationChangeException extends RuntimeException {

  private static final String MESSAGE_BASE_TEMPLATE = "%s action is prohibited for %s scope.";
  private static final String MESSAGE_FULL_TEMPLATE = MESSAGE_BASE_TEMPLATE + " %s";

  protected IllegalSpecificationChangeException(ActionType action, Scope scope) {
    super(String.format(MESSAGE_BASE_TEMPLATE, action.name().toLowerCase(), scope.getValue()));
  }

  protected IllegalSpecificationChangeException(ActionType action, Scope scope, String additionalInfo) {
    super(String.format(MESSAGE_FULL_TEMPLATE, action.name().toLowerCase(), scope.getValue(), additionalInfo));
  }

  public static IllegalSpecificationChangeException forDelete(Scope scope) {
    return new IllegalSpecificationChangeException(ActionType.DELETE, scope);
  }

  public static IllegalSpecificationChangeException forUpdate(Scope scope) {
    return new IllegalSpecificationChangeException(ActionType.UPDATE, scope);
  }

  public static IllegalSpecificationChangeException forTagChange(Scope scope) {
    return new IllegalSpecificationChangeException(ActionType.UPDATE, scope,
      AdditionalRestriction.TAG_CHANGE.getAdditionalInfo());
  }

  public static IllegalSpecificationChangeException forRequiredChange(Scope scope) {
    return new IllegalSpecificationChangeException(ActionType.UPDATE, scope,
      AdditionalRestriction.REQUIRED_CHANGE.getAdditionalInfo());
  }

  public static IllegalSpecificationChangeException forRepeatableChange(Scope scope) {
    return new IllegalSpecificationChangeException(ActionType.UPDATE, scope,
      AdditionalRestriction.REPEATABLE_CHANGE.getAdditionalInfo());
  }

  protected enum ActionType {
    DELETE, UPDATE
  }

  @Getter
  protected enum AdditionalRestriction {
    TAG_CHANGE("tag value is unmodifiable"),
    REQUIRED_CHANGE("required is unmodifiable"),
    REPEATABLE_CHANGE("repeatable is unmodifiable"),
    DEPRECATED_CHANGE("deprecated is unmodifiable")
    ;

    private final String additionalInfo;

    AdditionalRestriction(String additionalInfo) {
      this.additionalInfo = additionalInfo;
    }
  }
}
