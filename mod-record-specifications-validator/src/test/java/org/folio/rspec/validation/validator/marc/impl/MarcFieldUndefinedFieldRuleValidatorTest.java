package org.folio.rspec.validation.validator.marc.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.folio.rspec.domain.dto.SeverityType;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcField;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class MarcFieldUndefinedFieldRuleValidatorTest {

  @Mock
  private TranslationProvider translationProvider;
  @InjectMocks
  private MarcFieldUndefinedFieldRuleValidator validator;

  @Test
  public void validate_undefinedFieldShouldReturnValidationError() {
    var marcField = new MarcDataField(Reference.forTag("001"), List.of(), List.of());
    var fields = Collections.<String, List<MarcField>>singletonMap("001", Collections.singletonList(marcField));
    var specification = new SpecificationDto().fields(Collections.emptyList());

    when(translationProvider.format(validator.supportedRule().getCode())).thenReturn("message");

    var errors = validator.validate(fields, specification);

    assertEquals(1, errors.size());
    ValidationError error = errors.get(0);
    assertEquals(marcField.reference().toString(), error.getPath());
    assertEquals(validator.definitionType(), error.getDefinitionType());
    assertEquals(specification.getId(), error.getDefinitionId());
    assertEquals(SeverityType.WARN, error.getSeverity());
    assertEquals(validator.supportedRule().getCode(), error.getRuleCode());
    assertEquals("message", error.getMessage());
  }

  @Test
  public void validate_definedFieldShouldNotReturnValidationError() {
    var marcField = new MarcDataField(Reference.forTag("001"), List.of(), List.of());
    var fields = Collections.<String, List<MarcField>>singletonMap("001", Collections.singletonList(marcField));

    SpecificationFieldDto field = new SpecificationFieldDto().tag("001");
    var specification = new SpecificationDto().fields(Collections.singletonList(field));

    List<ValidationError> errors = validator.validate(fields, specification);
    assertEquals(0, errors.size());
  }
}
