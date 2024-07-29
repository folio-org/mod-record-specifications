package org.folio.rspec.service.validation.scope.field;

import java.util.function.Function;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.service.validation.scope.ScopeValidator;
import org.springframework.stereotype.Component;

@Component
public class FieldLocalScopeValidator extends ScopeValidator<SpecificationFieldChangeDto, Field> {

  @Override
  public Scope scope() {
    return Scope.LOCAL;
  }

  @Override
  protected Function<Field, Scope> scopeFunction() {
    return Field::getScope;
  }

}
