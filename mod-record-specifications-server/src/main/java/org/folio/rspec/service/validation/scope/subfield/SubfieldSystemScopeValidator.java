package org.folio.rspec.service.validation.scope.subfield;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.entity.Subfield;
import org.springframework.stereotype.Component;

@Component
public class SubfieldSystemScopeValidator extends SubfieldStandardScopeValidator {

  @Override
  public Scope scope() {
    return Scope.SYSTEM;
  }

  @Override
  protected Map<String, BiPredicate<SubfieldChangeDto, Subfield>> getChecks() {
    var checks = new HashMap<>(super.getChecks());
    checks.put(Subfield.REQUIRED_COLUMN, (dto, entity) -> dto.getRequired().equals(entity.isRequired()));
    return checks;
  }
}
