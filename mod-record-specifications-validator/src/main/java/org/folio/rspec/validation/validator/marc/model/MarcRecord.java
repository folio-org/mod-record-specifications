package org.folio.rspec.validation.validator.marc.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class MarcRecord {

  private final Map<String, List<MarcField>> allFields = new HashMap<>();
  private final List<MarcControlField> controlFields;
  private final List<MarcDataField> dataFields;

  public MarcRecord(List<MarcControlField> controlFields, List<MarcDataField> dataFields) {
    this.controlFields = controlFields;
    this.dataFields = dataFields;
    this.allFields.putAll(controlFields.stream().collect(Collectors.groupingBy(MarcField::tag)));
    this.allFields.putAll(dataFields.stream().collect(Collectors.groupingBy(MarcField::tag)));
  }

  public List<MarcControlField> controlFields() {
    return controlFields;
  }

  public List<MarcDataField> dataFields() {
    return dataFields;
  }
}
