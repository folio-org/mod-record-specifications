package org.folio.rspec.service.validation.scope.field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.entity.Field;
import org.springframework.stereotype.Service;

@Service
public class FieldStandardScopeValidator extends FieldLocalScopeValidator {

  @Override
  public Scope scope() {
    return Scope.STANDARD;
  }

  @Override
  protected Map<String, BiPredicate<SpecificationFieldChangeDto, Field>> getChecks() {
    var checks = new HashMap<>(super.getChecks());
    checks.put(Field.TAG_COLUMN, (dto, entity) -> dto.getTag().equals(entity.getTag()));
    checks.put(Field.LABEL_COLUMN, (dto, entity) -> dto.getLabel().equals(entity.getLabel()));
    checks.put(Field.REPEATABLE_COLUMN, (dto, entity) -> dto.getRepeatable().equals(entity.isRepeatable()));
    checks.put(Field.DEPRECATED_COLUMN, (dto, entity) -> dto.getDeprecated().equals(entity.isDeprecated()));
    return checks;
  }
}
