package org.folio.rspec.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationFieldDtoCollection;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.mapper.SpecificationFieldMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpecificationFieldService {

  private final FieldRepository fieldRepository;
  private final SpecificationFieldMapper specificationFieldMapper;

  public SpecificationFieldDtoCollection findSpecificationFields(UUID specificationId) {
    var specificationFieldDtos = fieldRepository.findBySpecificationId(specificationId).stream()
      .map(specificationFieldMapper::toDto)
      .toList();
    return new SpecificationFieldDtoCollection()
      .fields(specificationFieldDtos)
      .totalRecords(specificationFieldDtos.size());
  }

  public SpecificationFieldDto createLocalField(Specification specification, SpecificationFieldChangeDto createDto) {
    var fieldEntity = specificationFieldMapper.toEntity(createDto);
    fieldEntity.setSpecification(specification);
    fieldEntity.setScope(Scope.LOCAL);
    return specificationFieldMapper.toDto(fieldRepository.save(fieldEntity));
  }

  @Transactional
  public void deleteField(UUID id) {
    var fieldEntity = fieldRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forField(id));
    fieldRepository.delete(fieldEntity);
  }

  @Transactional
  public SpecificationFieldDto updateField(UUID id, SpecificationFieldChangeDto changeDto) {
    var fieldEntity = fieldRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forField(id));
    var updatedField = specificationFieldMapper.partialUpdate(changeDto, fieldEntity);

    return specificationFieldMapper.toDto(fieldRepository.save(updatedField));
  }
}
