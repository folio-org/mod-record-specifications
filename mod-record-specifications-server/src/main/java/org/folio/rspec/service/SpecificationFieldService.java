package org.folio.rspec.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.mapper.SpecificationFieldMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SpecificationFieldService {

  private final FieldRepository fieldRepository;
  private final SpecificationFieldMapper specificationFieldMapper;
  private final FieldIndicatorService indicatorService;

  public SpecificationFieldDtoCollection findSpecificationFields(UUID specificationId) {
    log.debug("findSpecificationFields::specificationId={}", specificationId);
    var specificationFieldDtos = fieldRepository.findBySpecificationId(specificationId).stream()
      .map(specificationFieldMapper::toDto)
      .toList();
    return new SpecificationFieldDtoCollection()
      .fields(specificationFieldDtos)
      .totalRecords(specificationFieldDtos.size());
  }

  public SpecificationFieldDto createLocalField(Specification specification, SpecificationFieldChangeDto createDto) {
    log.info("createLocalField::specificationId={}, dto={}", specification.getId(), createDto);
    var fieldEntity = specificationFieldMapper.toEntity(createDto);
    fieldEntity.setSpecification(specification);
    fieldEntity.setScope(Scope.LOCAL);
    return specificationFieldMapper.toDto(fieldRepository.save(fieldEntity));
  }

  @Transactional
  public void deleteField(UUID id) {
    log.info("deleteField::id={}", id);
    var fieldEntity = fieldRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forField(id));
    fieldRepository.delete(fieldEntity);
  }

  @Transactional
  public SpecificationFieldDto updateField(UUID id, SpecificationFieldChangeDto changeDto) {
    log.info("updateField::id={}, dto={}", id, changeDto);
    var fieldEntity = fieldRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forField(id));
    specificationFieldMapper.update(fieldEntity, changeDto);

    return specificationFieldMapper.toDto(fieldRepository.save(fieldEntity));
  }

  @Transactional
  public void syncFields(UUID specificationId, List<Field> fields) {
    log.info("syncFields::specificationId={}, fields number={}", specificationId, fields.size());
    log.trace("syncFields::specificationId={}, fields={}", specificationId, fields);
    fieldRepository.deleteBySpecificationId(specificationId);
    Map<String, Field> fieldByTags = new HashMap<>();
    for (Field field : fields) {
      fieldByTags.merge(field.getTag(), field, (field1, field2) -> field1.isDeprecated() ? field2 : field1);
      var specification = new Specification();
      specification.setId(specificationId);
      field.setSpecification(specification);
    }
    fieldRepository.saveAll(fieldByTags.values());
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
      field -> indicatorService.createLocalIndicator(field, createDto)
    );
  }

  private <T> T doForFieldOrFail(UUID fieldId, Function<Field, T> action) {
    return fieldRepository.findById(fieldId)
      .map(action)
      .orElseThrow(() -> ResourceNotFoundException.forField(fieldId));
  }
}
