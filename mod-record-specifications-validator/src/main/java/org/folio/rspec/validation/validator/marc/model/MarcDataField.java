package org.folio.rspec.validation.validator.marc.model;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record MarcDataField(
  Reference reference,
  List<MarcIndicator> indicators,
  List<MarcSubfield> subfields
) implements MarcField {

  @Override
  public boolean hasValue() {
    return !subfields.isEmpty() && subfields.stream()
      .anyMatch(marcSubfield -> StringUtils.isNotBlank(marcSubfield.value()));
  }

  public MarcIndicator indicator1() {
    return indicators.get(0);
  }

  public MarcIndicator indicator2() {
    return indicators.get(1);
  }
}
