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
class MarcSpecificationValidatorTest {

  @Mock
  private Converter<Object, MarcRecord> customConverter;

  @Test
  void validate_whenInvalidObject_throwsException() {
    SpecificationDto specificationDto = new SpecificationDto();
    Object rec = new Object();

    when(customConverter.convert(rec)).thenThrow(new RuntimeException("Error"));

    var validator = new MarcSpecificationValidator((key, args) -> "error", customConverter);
    assertThrows(IllegalArgumentException.class, () -> validator.validate(rec, specificationDto));
  }

  @Test
  void validate_whenUnsupportedType_throwsException() {
    SpecificationDto specificationDto = new SpecificationDto();
    String unsupportedRecord = "unsupported";

    var validator = new MarcSpecificationValidator((key, args) -> "error", null);

    assertThrows(IllegalArgumentException.class, () -> validator.validate(unsupportedRecord, specificationDto));
  }

}
