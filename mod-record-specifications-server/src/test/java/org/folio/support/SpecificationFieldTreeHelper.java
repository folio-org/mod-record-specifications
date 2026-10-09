package org.folio.support;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.domain.repository.SubfieldRepository;

/**
 * Looks up field/indicator/subfield/indicator-code rows by their natural key (tag, indicator
 * order, code) directly through the repositories, bypassing the HTTP DTOs. Intended to be reused
 * by any integration test that needs to assert (or set up) raw entity state - such as {@code
 * scope} - produced by a sync, a Liquibase data migration, or any other change, rather than each
 * test re-implementing its own lookups. Callers are responsible for running calls through {@code
 * executeInContext} like any other repository access in these tests, since lookups are
 * tenant-schema scoped.
 */
@RequiredArgsConstructor
public class SpecificationFieldTreeHelper {

  private final FieldRepository fieldRepository;
  private final IndicatorRepository indicatorRepository;
  private final IndicatorCodeRepository indicatorCodeRepository;
  private final SubfieldRepository subfieldRepository;

  public Optional<Field> findField(UUID specificationId, String tag) {
    return fieldRepository.findBySpecificationIdAndTag(specificationId, tag);
  }

  public Field findFieldOrFail(UUID specificationId, String tag) {
    return findField(specificationId, tag)
      .orElseThrow(() -> new AssertionError("Field " + tag + " not found"));
  }

  public List<Subfield> subfieldsOf(UUID fieldId) {
    return subfieldRepository.findByFieldId(fieldId);
  }

  public Optional<Subfield> findSubfield(UUID fieldId, String code) {
    return subfieldsOf(fieldId).stream().filter(subfield -> code.equals(subfield.getCode())).findFirst();
  }

  public Subfield findSubfieldOrFail(UUID fieldId, String code) {
    return findSubfield(fieldId, code)
      .orElseThrow(() -> new AssertionError("Subfield " + code + " not found on field " + fieldId));
  }

  public List<Indicator> indicatorsOf(UUID fieldId) {
    return indicatorRepository.findByFieldId(fieldId);
  }

  public Optional<Indicator> findIndicator(UUID fieldId, int order) {
    return indicatorsOf(fieldId).stream().filter(indicator -> order == indicator.getOrder()).findFirst();
  }

  public Indicator findIndicatorOrFail(UUID fieldId, int order) {
    return findIndicator(fieldId, order)
      .orElseThrow(() -> new AssertionError("Indicator order " + order + " not found on field " + fieldId));
  }

  public List<IndicatorCode> codesOf(UUID indicatorId) {
    return indicatorCodeRepository.findByIndicatorId(indicatorId);
  }

  public Optional<IndicatorCode> findIndicatorCode(UUID indicatorId, String code) {
    return codesOf(indicatorId).stream().filter(c -> code.equals(c.getCode())).findFirst();
  }
}
