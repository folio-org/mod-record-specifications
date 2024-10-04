package org.folio.rspec.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.SubfieldDtoCollection;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.integration.kafka.EventProducer;
import org.folio.rspec.service.mapper.SubfieldMapper;
import org.folio.rspec.service.validation.scope.ScopeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SubfieldService {

  private final SubfieldRepository repository;
  private final SubfieldMapper mapper;
  private final EventProducer<UUID, SpecificationUpdatedEvent> eventProducer;

  private final Map<Scope, ScopeValidator<SubfieldChangeDto, Subfield>> validators = new EnumMap<>(Scope.class);

  public SubfieldDtoCollection findFieldSubfields(UUID fieldId) {
    log.debug("findFieldSubfields::fieldId={}", fieldId);
    var subfieldDtos = repository.findByFieldId(fieldId).stream()
      .map(mapper::toDto)
      .toList();
    return new SubfieldDtoCollection().subfields(subfieldDtos).totalRecords(subfieldDtos.size());
  }

  public SubfieldDto saveSubfield(Field field, SubfieldDto dto) {
    log.debug("saveSubfield::fieldId={}, dto={}", field.getId(), dto);
    var entity = mapper.toEntity(dto);
    entity.setField(field);
    var created = repository.save(entity);
    return mapper.toDto(created);
  }

  public SubfieldDto createLocalSubfield(Field field, SubfieldChangeDto createDto) {
    log.info("createLocalSubfield::fieldId={}, dto={}", field.getId(), createDto);
    var entity = mapper.toEntity(createDto);
    entity.setField(field);
    entity.setScope(Scope.LOCAL);
    var created = repository.save(entity);
    return mapper.toDto(created);
  }

  @Transactional
  public SubfieldDto updateSubfield(UUID id, SubfieldChangeDto changeDto) {
    log.info("updateSubfield::subfieldId={}, dto={}", id, changeDto);
    var subfieldEntity = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forSubfield(id));
    var scope = subfieldEntity.getScope();
    Optional.ofNullable(validators.get(scope))
      .ifPresent(validator -> validator.validateChange(changeDto, subfieldEntity));
    mapper.update(subfieldEntity, changeDto);

    var dto = mapper.toDto(repository.save(subfieldEntity));
    eventProducer.sendEvent(subfieldEntity.getField().getSpecification().getId());
    return dto;
  }

  @Transactional
  public void deleteSubfield(UUID id) {
    log.info("deleteSubfield::subfieldId={}", id);
    var subfieldEntity = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forSubfield(id));
    if (subfieldEntity.getScope() != Scope.LOCAL) {
      throw ScopeModificationNotAllowedException.forDelete(subfieldEntity.getScope(), Subfield.SUBFIELD_TABLE_NAME);
    }
    var specificationId = subfieldEntity.getField().getSpecification().getId();
    repository.delete(subfieldEntity);
    eventProducer.sendEvent(specificationId);
  }

  @Autowired
  public void setValidators(List<ScopeValidator<SubfieldChangeDto, Subfield>> validators) {
    validators.forEach(validator -> this.validators.put(validator.scope(), validator));
  }
}
