package org.folio.rspec.validation.validator.marc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.validation.converter.Converter;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class MarcSpecificationValidatorTest {

  @Mock
  private Converter<Object, MarcRecord> customConverter;

  @Test
  void validate_whenInvalidObject_throwsException() {
    SpecificationDto specificationDto = new SpecificationDto();
    Object record = new Object();

    when(customConverter.convert(record)).thenThrow(new RuntimeException("Error"));

    var validator = new MarcSpecificationValidator((key, args) -> "error", customConverter);
    assertThrows(IllegalArgumentException.class, () -> validator.validate(record, specificationDto));
  }

  @Test
  void validate_whenUnsupportedType_throwsException() {
    SpecificationDto specificationDto = new SpecificationDto();
    String record = "unsupported";

    var validator = new MarcSpecificationValidator((key, args) -> "error", null);

    assertThrows(IllegalArgumentException.class, () -> validator.validate(record, specificationDto));
  }

}
