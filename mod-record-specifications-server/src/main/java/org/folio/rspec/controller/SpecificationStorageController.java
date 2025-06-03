package org.folio.rspec.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationFieldDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.rest.resource.SpecificationStorageApi;
import org.folio.rspec.service.SpecificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class SpecificationStorageController implements SpecificationStorageApi {

  private final SpecificationService specificationService;

  @Override
  public ResponseEntity<Void> syncSpecification(UUID specificationId) {
    specificationService.sync(specificationId);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @Override
  public ResponseEntity<SpecificationFieldDto> createSpecificationLocalField(UUID specificationId,
                                                                             SpecificationFieldChangeDto createDto) {
    SpecificationFieldDto fieldDto = specificationService.createLocalField(specificationId, createDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(fieldDto);
  }

  @Override
  public ResponseEntity<SpecificationFieldDtoCollection> getSpecificationFields(UUID specificationId) {
    return ResponseEntity.ok(specificationService.findSpecificationFields(specificationId));
  }

  @Override
  public ResponseEntity<SpecificationRuleDtoCollection> getSpecificationRules(UUID specificationId) {
    return ResponseEntity.ok(specificationService.findSpecificationRules(specificationId));
  }

  @Override
  public ResponseEntity<SpecificationDtoCollection> getSpecifications(Family family, FamilyProfile profile,
                                                                      IncludeParam include, Integer limit,
                                                                      Integer offset) {
    var specifications = specificationService.findSpecifications(family, profile, include, limit, offset);
    return ResponseEntity.ok(specifications);
  }

  @Override
  public ResponseEntity<SpecificationDto> getSpecification(UUID specificationId, IncludeParam include) {
    var specification = specificationService.getSpecificationById(specificationId, include);
    return ResponseEntity.ok(specification);
  }

  @Override
  public ResponseEntity<Void> toggleSpecificationRule(UUID specificationId, UUID id,
                                                      ToggleSpecificationRuleDto toggleSpecificationRuleDto) {
    specificationService.toggleSpecificationRule(specificationId, id, toggleSpecificationRuleDto);
    return ResponseEntity.noContent().build();
  }
}
