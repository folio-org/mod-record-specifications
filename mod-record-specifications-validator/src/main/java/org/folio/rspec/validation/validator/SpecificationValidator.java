package org.folio.rspec.validation.validator;

import java.util.List;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;

public interface SpecificationValidator {

  List<ValidationError> validate(Object record, SpecificationDto specification);

}
