package org.folio.rspec.validation.converter.marc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.folio.rspec.validation.converter.Converter;
import org.folio.rspec.validation.validator.marc.model.MarcControlField;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class Marc4jConverter implements Converter<Record, MarcRecord> {

  @Override
  public MarcRecord convert(Record record) {
    var controlFields = convertControlFields(record);
    var dataFields = convertDataFields(record);

    return new MarcRecord(controlFields, dataFields);
  }

  private List<MarcControlField> convertControlFields(Record record) {
    var controlFields = new ArrayList<MarcControlField>();
    controlFields.add(convertLeader(record));

    record.getControlFields().stream()
      .collect(Collectors.groupingBy(VariableField::getTag))
      .forEach((tag, fields) -> fields.forEach(field ->
        controlFields.add(convertControlField(fields, field))));
    return controlFields;
  }

  private MarcControlField convertLeader(Record record) {
    return new MarcControlField(Reference.forTag("000"), record.getLeader().toString());
  }

  private MarcControlField convertControlField(List<ControlField> fields, ControlField field) {
    var reference = Reference.forTag(field.getTag(), fields.indexOf(field));
    return new MarcControlField(reference, field.getData());
  }

  private List<MarcDataField> convertDataFields(Record record) {
    var dataFields = new ArrayList<MarcDataField>();

    record.getDataFields().stream()
      .collect(Collectors.groupingBy(VariableField::getTag))
      .forEach((tag, fields) -> fields.forEach(field -> {
        var marcDataField = toMarcDataField(field, fields.indexOf(field));
        dataFields.add(marcDataField);
      }));

    return dataFields;
  }

  private MarcDataField toMarcDataField(DataField field, int fieldIndex) {
    var reference = Reference.forTag(field.getTag(), fieldIndex);
    var indicators = convertIndicators(field, reference);
    var marcSubfields = convertSubfields(reference, field.getSubfields());

    return new MarcDataField(reference, indicators, marcSubfields);
  }

  private List<MarcSubfield> convertSubfields(Reference parentReference, List<Subfield> subfields) {
    var marcSubfields = new ArrayList<MarcSubfield>();
    subfields.stream()
      .collect(Collectors.groupingBy(Subfield::getCode))
      .forEach((code, subfieldList) -> subfieldList.forEach(subfield ->
        marcSubfields.add(toMarcSubfield(parentReference, subfieldList, subfield))));
    return marcSubfields;
  }

  private List<MarcIndicator> convertIndicators(DataField field, Reference reference) {
    return List.of(
      toMarcIndicator(reference, field.getIndicator1(), 1),
      toMarcIndicator(reference, field.getIndicator2(), 2));
  }

  private MarcIndicator toMarcIndicator(Reference fieldReference, char indicatorValue, int indicatorIndex) {
    var reference = Reference.forIndicator(fieldReference, indicatorIndex);
    return new MarcIndicator(reference, indicatorValue);
  }

  private MarcSubfield toMarcSubfield(Reference parentReference, List<Subfield> subfieldList, Subfield subfield) {
    var reference = Reference.forSubfield(parentReference, subfield.getCode(), subfieldList.indexOf(subfield));
    return new MarcSubfield(reference, subfield.getData());
  }
}
