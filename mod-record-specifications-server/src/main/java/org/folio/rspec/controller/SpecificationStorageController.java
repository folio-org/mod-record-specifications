package org.folio.rspec.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
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
                                                                   String include, Integer limit, Integer offset) {
    if (StringUtils.isNotBlank(include)) {
      return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
    var specifications = specificationService.findSpecifications(family, profile, include, limit, offset);
    return ResponseEntity.ok(specifications);
  }
}
