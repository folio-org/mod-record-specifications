package org.folio.rspec.service.validation.scope;

import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;

public interface ScopeValidator<D, E> {

  void validateChange(D dto, E entity) throws ScopeModificationNotAllowedException;

  Scope scope();
}
