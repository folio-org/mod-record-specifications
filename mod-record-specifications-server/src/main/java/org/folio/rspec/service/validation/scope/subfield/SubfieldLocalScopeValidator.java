package org.folio.rspec.service.validation.scope.subfield;

import java.util.function.Function;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.service.validation.scope.ScopeValidator;
import org.springframework.stereotype.Component;

@Component
public class SubfieldLocalScopeValidator extends ScopeValidator<SubfieldChangeDto, Subfield> {

  @Override
  public Scope scope() {
    return Scope.LOCAL;
  }

  @Override
  protected Function<Subfield, Scope> scopeFunction() {
    return Subfield::getScope;
  }

}
