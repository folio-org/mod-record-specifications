package org.folio.rspec.service;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.Subfield;
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

  private Map<Scope, ScopeValidator<SpecificationFieldChangeDto, Field>> fieldValidators = new EnumMap<>(Scope.class);

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
  public void syncFields(Specification specification, Collection<Field> fields) {
    log.info("syncFields::specificationId={}, fields number={}", specification.getId(), fields.size());
    log.trace("syncFields::specificationId={}, fields={}", specification.getId(), fields);
    fieldRepository.deleteBySpecificationId(specification.getId());
    fieldRepository.saveAll(fields);
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
