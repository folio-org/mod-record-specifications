package org.folio.rspec.service.validation.scope.field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.service.validation.scope.ScopeValidator;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class FieldLocalScopeValidator implements ScopeValidator<SpecificationFieldChangeDto, Field> {

  protected final Map<String, BiPredicate<SpecificationFieldChangeDto, Field>> checks = new HashMap<>();

  @Override
  public void validateChange(SpecificationFieldChangeDto dto, Field entity) {
    log.debug("validateChange::scope={}, dto={}", scope(), dto);
    if (!scope().equals(entity.getScope())) {
      throw new IllegalStateException("Illegal scope for the entity: " + entity);
    }
    checks.forEach((field, check) -> {
      if (!check.test(dto, entity)) {
        throw ScopeModificationNotAllowedException.forUpdate(scope(), field);
      }
    });
  }

  @Override
  public Scope scope() {
    return Scope.LOCAL;
  }
}
