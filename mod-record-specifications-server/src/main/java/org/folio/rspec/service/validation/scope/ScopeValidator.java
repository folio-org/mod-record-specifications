package org.folio.rspec.service.validation.scope;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;

@Log4j2
public abstract class ScopeValidator<D, E> {

  public void validateChange(D dto, E entity) throws ScopeModificationNotAllowedException {
    log.debug("validateChange::scope={}, dto={}", scope(), dto);
    if (!scope().equals(scopeFunction().apply(entity))) {
      throw new IllegalStateException("Illegal scope for the entity: " + entity);
    }
    getChecks().forEach((field, check) -> {
      if (!check.test(dto, entity)) {
        throw ScopeModificationNotAllowedException.forUpdate(scope(), field);
      }
    });
  }

  public abstract Scope scope();

  protected abstract Function<E, Scope> scopeFunction();

  protected Map<String, BiPredicate<D, E>> getChecks() {
    return Collections.emptyMap();
  }
}
