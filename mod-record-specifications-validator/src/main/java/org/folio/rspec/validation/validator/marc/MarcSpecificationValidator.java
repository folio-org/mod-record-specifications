package org.folio.rspec.validation.validator.marc;

import java.util.List;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.ValidationError;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.converter.Converter;
import org.folio.rspec.validation.converter.marc.Marc4jConverter;
import org.folio.rspec.validation.validator.SpecificationValidator;
import org.folio.rspec.validation.validator.marc.impl.MarcRecordRuleValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.marc4j.marc.Record;

public class MarcSpecificationValidator implements SpecificationValidator {

  private final MarcRecordRuleValidator marcRecordRuleValidator;
  private final Converter<Record, MarcRecord> defaultConverter;
  private final Converter<Object, MarcRecord> converter;

  public MarcSpecificationValidator(TranslationProvider translationProvider, Converter<Object, MarcRecord> converter) {
    this.marcRecordRuleValidator = new MarcRecordRuleValidator(translationProvider);
    this.converter = converter;
    this.defaultConverter = new Marc4jConverter();
  }

  @Override
  public List<ValidationError> validate(Object record, SpecificationDto specification) {
    MarcRecord marcRecord;
    if (record instanceof MarcRecord) {
      marcRecord = (MarcRecord) record;
    } else if (record instanceof Record) {
      marcRecord = defaultConverter.convert((Record) record);
    } else if (converter != null) {
      try {
        marcRecord = converter.convert(record);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid converter: ", e);
      }
    } else {
      throw new IllegalArgumentException("Unexpected record type.");
    }

    return marcRecordRuleValidator.validate(marcRecord, specification);
  }

}
