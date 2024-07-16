package org.folio.rspec.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.MarcSpecificationValidator;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SpecificationGuidedValidatorTest {

  @Mock
  private TranslationProvider translationProvider;

  @Test
  void validate_givenValidRecordAndSpecification_usesCorrectValidator() {
    var family = Family.MARC;
    var specificationDto = new SpecificationDto().family(family);
    var record = new Object();
    var errors = Collections.singletonList(
      ValidationError.builder()
        .path("path")
        .definitionType(DefinitionType.FIELD)
        .definitionId(UUID.randomUUID())
        .severity(SeverityType.ERROR)
        .ruleCode("ruleCode")
        .message("message")
        .build());

    try (var construction = Mockito.mockConstruction(MarcSpecificationValidator.class,
      (marcValidator, context) -> when(marcValidator.validate(any(), any())).thenReturn(errors))) {
      var validator = new SpecificationGuidedValidator(translationProvider, source -> source);
      var result = validator.validate(record, specificationDto);
      assertEquals(1, construction.constructed().size());
      assertEquals(result, errors);
    }
  }

}
