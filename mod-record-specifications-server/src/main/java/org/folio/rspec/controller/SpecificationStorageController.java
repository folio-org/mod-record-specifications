package org.folio.rspec.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
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
  public ResponseEntity<SpecificationDtoCollection> getSpecifications(Family family, FamilyProfile profile,
                                                                      IncludeParam include, Integer limit,
                                                                      Integer offset) {
    if (IncludeParam.REQUIRED_FIELDS == include) {
      return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
    var specifications = specificationService.findSpecifications(family, profile, include, limit, offset);
    return ResponseEntity.ok(specifications);
  }

  @Override
  public ResponseEntity<SpecificationRuleDtoCollection> getSpecificationRules(UUID id) {
    return ResponseEntity.ok(specificationService.findSpecificationRules(id));
  }

  @Override
  public ResponseEntity<Void> toggleSpecificationRule(UUID specificationId, UUID ruleId,
                                                      ToggleSpecificationRuleDto toggleSpecificationRuleDto) {
    specificationService.toggleSpecificationRule(specificationId, ruleId, toggleSpecificationRuleDto);
    return ResponseEntity.noContent().build();
  }
}
