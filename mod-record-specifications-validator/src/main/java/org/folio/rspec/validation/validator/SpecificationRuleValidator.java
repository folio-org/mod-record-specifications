package org.folio.rspec.validation.validator;

import java.util.List;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.ValidationError;

public interface SpecificationRuleValidator<T, S> {

  List<ValidationError> validate(T object, S specification);

  SpecificationRuleCode supportedRule();

  DefinitionType definitionType();

  default String ruleCode() {
    return supportedRule().getCode();
  }

  default SeverityType severity() {
    return SeverityType.WARN;
  }
}
