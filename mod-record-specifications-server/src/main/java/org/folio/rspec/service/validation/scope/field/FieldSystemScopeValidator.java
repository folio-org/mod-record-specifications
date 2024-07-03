package org.folio.rspec.service.validation.scope.field;

import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.springframework.stereotype.Service;

@Service
public class FieldSystemScopeValidator extends FieldStandardScopeValidator {

  public FieldSystemScopeValidator() {
    super();
    checks.put(Field.REQUIRED_COLUMN, (dto, entity) -> dto.getRequired().equals(entity.isRequired()));
  }

  @Override
  public Scope scope() {
    return Scope.SYSTEM;
  }
}
