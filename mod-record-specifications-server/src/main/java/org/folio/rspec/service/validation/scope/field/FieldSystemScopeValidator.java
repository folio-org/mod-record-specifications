package org.folio.rspec.service.validation.scope.field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.entity.Field;
import org.springframework.stereotype.Service;

@Service
public class FieldSystemScopeValidator extends FieldStandardScopeValidator {

  @Override
  public Scope scope() {
    return Scope.SYSTEM;
  }

  @Override
  protected Map<String, BiPredicate<SpecificationFieldChangeDto, Field>> getChecks() {
    var checks = new HashMap<>(super.getChecks());
    checks.put(Field.REQUIRED_COLUMN, (dto, entity) -> dto.getRequired().equals(entity.isRequired()));
    return checks;
  }
}
