package org.folio.rspec.service;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationFieldDtoCollection;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.SubfieldDtoCollection;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.integration.kafka.EventProducer;
import org.folio.rspec.service.mapper.FieldMapper;
import org.folio.rspec.service.validation.resource.FieldValidator;
import org.folio.rspec.service.validation.scope.ScopeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SpecificationFieldService {

  private final FieldRepository fieldRepository;
  private final FieldMapper fieldMapper;
  private final FieldIndicatorService indicatorService;
  private final SubfieldService subfieldService;
  private final FieldValidator fieldValidator;
  private final EventProducer<UUID, SpecificationUpdatedEvent> eventProducer;

  private final Map<Scope, ScopeValidator<SpecificationFieldChangeDto, Field>> fieldValidators =
    new EnumMap<>(Scope.class);

  public SpecificationFieldDtoCollection findSpecificationFields(UUID specificationId) {
    log.debug("findSpecificationFields::specificationId={}", specificationId);
    var specificationFieldDtos = fieldRepository.findBySpecificationId(specificationId).stream()
      .map(fieldMapper::toDto)
      .toList();
    return new SpecificationFieldDtoCollection()
      .fields(specificationFieldDtos)
      .totalRecords(specificationFieldDtos.size());
  }

  public SpecificationFieldDtoCollection findSpecificationFields(UUID specificationId, boolean requiredFilter) {
    log.debug("findSpecificationFields::specificationId={}, requiredFilter={}", specificationId, requiredFilter);
    var specificationFieldDtos = fieldRepository.findBySpecificationIdAndRequired(specificationId, requiredFilter)
      .stream()
      .map(fieldMapper::toDto)
      .toList();
    return new SpecificationFieldDtoCollection()
      .fields(specificationFieldDtos)
      .totalRecords(specificationFieldDtos.size());
  }

  public SpecificationFieldDto createLocalField(Specification specification, SpecificationFieldChangeDto createDto) {
    log.info("createLocalField::specificationId={}, dto={}", specification.getId(), createDto);
    var fieldEntity = fieldMapper.toEntity(createDto);
    fieldEntity.setSpecification(specification);
    fieldEntity.setScope(Scope.LOCAL);
    return fieldMapper.toDto(fieldRepository.save(fieldEntity));
  }

  @Transactional
  public void deleteField(UUID id) {
    log.info("deleteField::id={}", id);
    var fieldEntity = fieldRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forField(id));
    if (fieldEntity.getScope() != Scope.LOCAL) {
      throw ScopeModificationNotAllowedException.forDelete(fieldEntity.getScope(), Field.FIELD_TABLE_NAME);
    }
    var specificationId = fieldEntity.getSpecification().getId();
    fieldRepository.delete(fieldEntity);
    eventProducer.sendEvent(specificationId);
  }

  @Transactional
  public SpecificationFieldDto updateField(UUID id, SpecificationFieldChangeDto changeDto) {
    log.info("updateField::id={}, dto={}", id, changeDto);
    var fieldEntity = fieldRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forField(id));
    var scope = fieldEntity.getScope();
    Optional.ofNullable(fieldValidators.get(scope))
      .ifPresent(validator -> validator.validateChange(changeDto, fieldEntity));
    fieldMapper.update(fieldEntity, changeDto);

    var dto = fieldMapper.toDto(fieldRepository.save(fieldEntity));
    eventProducer.sendEvent(fieldEntity.getSpecification().getId());
    return dto;
  }

  @Transactional
  public FieldIndicatorDtoCollection findFieldIndicators(UUID fieldId) {
    log.debug("findFieldIndicators::fieldId={}", fieldId);
    return doForFieldOrFail(fieldId,
      field -> indicatorService.findFieldIndicators(fieldId)
    );
  }

  @Transactional
  public FieldIndicatorDto createLocalIndicator(UUID fieldId, FieldIndicatorChangeDto createDto) {
    log.debug("createLocalIndicator::fieldId={}, createDto={}", fieldId, createDto);
    return doForFieldOrFail(fieldId,
      field -> {
        fieldValidator.validateFieldResourceCreate(field, Indicator.INDICATOR_TABLE_NAME);
        var indicator = indicatorService.createLocalIndicator(field, createDto);
        eventProducer.sendEvent(field.getSpecification().getId());
        return indicator;
      }
    );
  }

  public SubfieldDtoCollection findFieldSubfields(UUID fieldId) {
    log.debug("findFieldSubfields::fieldId={}", fieldId);
    return doForFieldOrFail(fieldId,
      field -> subfieldService.findFieldSubfields(fieldId)
    );
  }

  @Transactional
  public SubfieldDto saveSubfield(UUID specificationId, String fieldTag, SubfieldDto dto) {
    log.debug("saveSubfield::dto={}", dto);
    return doForFieldOrFail(specificationId, fieldTag,
      field -> {
        var saved = subfieldService.saveSubfield(field, dto);
        eventProducer.sendEvent(field.getSpecification().getId());
        return saved;
      }
    );
  }

  public SubfieldDto createLocalSubfield(UUID fieldId, SubfieldChangeDto createDto) {
    log.debug("createLocalSubfield::fieldId={}, createDto={}", fieldId, createDto);
    return doForFieldOrFail(fieldId,
      field -> {
        fieldValidator.validateFieldResourceCreate(field, Subfield.SUBFIELD_TABLE_NAME);
        var dto = subfieldService.createLocalSubfield(field, createDto);
        eventProducer.sendEvent(field.getSpecification().getId());
        return dto;
      }
    );
  }

  @Transactional
  public void syncFields(Specification specification, Collection<Field> fields, boolean preserveLocal,
                         SpecificationMetadata specificationMetadata) {
    log.info("syncFields::specificationId={}, fields number={}, preserveLocal={}",
      specification.getId(), fields.size(), preserveLocal);
    log.trace("syncFields::specificationId={}, fields={}", specification.getId(), fields);
    if (preserveLocal) {
      reconcileFields(specification, fields, specificationMetadata);
    } else {
      fieldRepository.deleteBySpecificationId(specification.getId());
      fieldRepository.saveAll(fields);
    }
  }

  /**
   * Reconciles persisted fields with the freshly computed spec-driven {@code incomingFields} instead of wiping
   * everything: a tag/order/code that the spec now defines always overrides whatever is currently stored there
   * (even if it was LOCAL), while a LOCAL definition the spec doesn't know about is left untouched. Indicators
   * have no scope column of their own, so "does the spec know about this order" is read off
   * {@code specificationMetadata} instead (see docs/marc-spec-corrections.md).
   */
  private void reconcileFields(Specification specification, Collection<Field> incomingFields,
                               SpecificationMetadata specificationMetadata) {
    var existingByTag = fieldRepository.findBySpecificationId(specification.getId()).stream()
      .collect(Collectors.toMap(Field::getTag, Function.identity()));
    var incomingByTag = incomingFields.stream()
      .collect(Collectors.toMap(Field::getTag, Function.identity()));

    deleteStaleFields(existingByTag, incomingByTag);

    var fieldsMetadata = specificationMetadata == null ? null : specificationMetadata.getFields();
    for (var incoming : incomingByTag.values()) {
      var existing = existingByTag.get(incoming.getTag());
      if (existing != null) {
        var fieldMetadata = fieldsMetadata == null ? null : fieldsMetadata.get(incoming.getTag());
        preserveEditableFieldOverrides(existing, incoming);
        reconcileSubfields(existing, incoming);
        carryOverLocalIndicators(existing, incoming, fieldMetadata);
        fieldRepository.deleteById(existing.getId());
      }
      fieldRepository.save(incoming);
    }
  }

  private void deleteStaleFields(Map<String, Field> existingByTag, Map<String, Field> incomingByTag) {
    var staleFieldIds = existingByTag.values().stream()
      .filter(existing -> !incomingByTag.containsKey(existing.getTag()))
      .filter(existing -> existing.getScope() != Scope.LOCAL)
      .map(Field::getId)
      .toList();
    if (!staleFieldIds.isEmpty()) {
      fieldRepository.deleteAllById(staleFieldIds);
    }
  }

  /**
   * A STANDARD/SYSTEM field's {@code url} and (STANDARD-only) {@code required} can be edited
   * through the API (see {@code FieldStandardScopeValidator}/{@code FieldSystemScopeValidator}).
   * Those are operator policy choices, not spec facts, so they're kept across a resync as long as
   * they don't contradict an invariant the spec itself enforces - namely that a deprecated field
   * never has a url. A LOCAL field has nothing scope-restricted to preserve here; it's either kept
   * wholesale (not in the incoming spec) or fully superseded by the spec (same key) elsewhere.
   */
  private void preserveEditableFieldOverrides(Field existing, Field incoming) {
    if (existing.getScope() == Scope.LOCAL) {
      return;
    }
    if (!incoming.isDeprecated() && !Objects.equals(existing.getUrl(), incoming.getUrl())) {
      incoming.setUrl(existing.getUrl());
    }
    if (existing.getScope() == Scope.STANDARD && existing.isRequired() != incoming.isRequired()) {
      incoming.setRequired(existing.isRequired());
    }
  }

  private void reconcileSubfields(Field existing, Field incoming) {
    var incomingByCode = incoming.getSubfields().stream()
      .collect(Collectors.toMap(Subfield::getCode, Function.identity()));
    for (var existingSubfield : existing.getSubfields()) {
      var incomingSubfield = incomingByCode.get(existingSubfield.getCode());
      if (incomingSubfield == null) {
        if (existingSubfield.getScope() == Scope.LOCAL) {
          incoming.getSubfields().add(copySubfield(existingSubfield, incoming));
        }
      } else if (existingSubfield.getScope() == Scope.STANDARD
          && existingSubfield.isRequired() != incomingSubfield.isRequired()) {
        // SubfieldStandardScopeValidator only leaves "required" editable; SYSTEM leaves nothing.
        incomingSubfield.setRequired(existingSubfield.isRequired());
      }
    }
  }

  private void carryOverLocalIndicators(Field existing, Field incoming, FieldMetadata fieldMetadata) {
    var incomingByOrder = incoming.getIndicators().stream()
      .collect(Collectors.toMap(Indicator::getOrder, Function.identity()));
    for (var existingIndicator : existing.getIndicators()) {
      var incomingIndicator = incomingByOrder.get(existingIndicator.getOrder());
      if (incomingIndicator == null) {
        if (!isIndicatorKnownToSpec(fieldMetadata, existingIndicator.getOrder())) {
          incoming.getIndicators().add(copyIndicator(existingIndicator, incoming));
        }
      } else {
        carryOverLocalIndicatorCodes(existingIndicator, incomingIndicator);
      }
    }
  }

  private boolean isIndicatorKnownToSpec(FieldMetadata fieldMetadata, Integer order) {
    return fieldMetadata != null && fieldMetadata.indicators() != null
      && fieldMetadata.indicators().containsKey(String.valueOf(order));
  }

  private void carryOverLocalIndicatorCodes(Indicator existing, Indicator incoming) {
    var incomingCodes = incoming.getCodes().stream().map(IndicatorCode::getCode).collect(Collectors.toSet());
    existing.getCodes().stream()
      .filter(code -> code.getScope() == Scope.LOCAL)
      .filter(code -> !incomingCodes.contains(code.getCode()))
      .forEach(code -> incoming.getCodes().add(copyIndicatorCode(code, incoming)));
  }

  private Subfield copySubfield(Subfield source, Field newField) {
    var copy = new Subfield();
    copy.setCode(source.getCode());
    copy.setLabel(source.getLabel());
    copy.setRepeatable(source.isRepeatable());
    copy.setRequired(source.isRequired());
    copy.setDeprecated(source.isDeprecated());
    copy.setScope(source.getScope());
    copy.setField(newField);
    return copy;
  }

  private Indicator copyIndicator(Indicator source, Field newField) {
    var copy = new Indicator();
    copy.setOrder(source.getOrder());
    copy.setLabel(source.getLabel());
    copy.setField(newField);
    copy.setCodes(source.getCodes().stream().map(code -> copyIndicatorCode(code, copy)).toList());
    return copy;
  }

  private IndicatorCode copyIndicatorCode(IndicatorCode source, Indicator newIndicator) {
    var copy = new IndicatorCode();
    copy.setCode(source.getCode());
    copy.setLabel(source.getLabel());
    copy.setDeprecated(source.isDeprecated());
    copy.setScope(source.getScope());
    copy.setIndicator(newIndicator);
    return copy;
  }

  @Autowired
  public void setFieldValidators(List<ScopeValidator<SpecificationFieldChangeDto, Field>> fieldValidators) {
    fieldValidators.forEach(validator -> this.fieldValidators.put(validator.scope(), validator));
  }

  private <T> T doForFieldOrFail(UUID fieldId, Function<Field, T> action) {
    return fieldRepository.findById(fieldId)
      .map(action)
      .orElseThrow(() -> ResourceNotFoundException.forField(fieldId));
  }

  private <T> T doForFieldOrFail(UUID specificationId, String fieldTag, Function<Field, T> action) {
    return fieldRepository.findBySpecificationIdAndTag(specificationId, fieldTag)
      .map(action)
      .orElseThrow(() -> ResourceNotFoundException.forField(specificationId, fieldTag));
  }
}
