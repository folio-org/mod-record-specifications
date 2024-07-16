package org.folio.rspec.validation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.i18n.TranslationProviderDelegate;
import org.folio.rspec.validation.converter.Converter;
import org.folio.rspec.validation.validator.SpecificationValidator;
import org.folio.rspec.validation.validator.marc.MarcSpecificationValidator;

public class SpecificationGuidedValidator {

  private final Map<Family, SpecificationValidator> validators;

  public SpecificationGuidedValidator(TranslationProvider translationProvider) {
    this(translationProvider, null);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public SpecificationGuidedValidator(TranslationProvider translationProvider, Converter converter) {
    var translationProviderDelegate = new TranslationProviderDelegate(translationProvider);
    this.validators = Map.of(Family.MARC, new MarcSpecificationValidator(translationProviderDelegate, converter));
  }

  public Collection<ValidationError> validate(Object record, SpecificationDto specification) {
    var family = specification.getFamily();
    var familyValidator = Optional.of(validators.get(family))
      .orElseThrow(() -> new UnsupportedOperationException("%s validator not found".formatted(family)));
    return familyValidator.validate(record, specification);
  }
}
