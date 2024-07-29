package org.folio.rspec.service.validation.scope.subfield;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.entity.Subfield;
import org.springframework.stereotype.Component;

@Component
public class SubfieldStandardScopeValidator extends SubfieldLocalScopeValidator {

  @Override
  public Scope scope() {
    return Scope.STANDARD;
  }

  @Override
  protected Map<String, BiPredicate<SubfieldChangeDto, Subfield>> getChecks() {
    var checks = new HashMap<>(super.getChecks());
    checks.put(Subfield.CODE_COLUMN, (dto, entity) -> dto.getCode().equals(entity.getCode()));
    checks.put(Subfield.LABEL_COLUMN, (dto, entity) -> dto.getLabel().equals(entity.getLabel()));
    checks.put(Subfield.REPEATABLE_COLUMN, (dto, entity) -> dto.getRepeatable().equals(entity.isRepeatable()));
    checks.put(Subfield.DEPRECATED_COLUMN, (dto, entity) -> dto.getDeprecated().equals(entity.isDeprecated()));
    return checks;
  }
}
