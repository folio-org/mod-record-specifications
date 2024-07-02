package org.folio.rspec.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.rest.resource.SpecificationStorageFieldsApi;
import org.folio.rspec.service.SpecificationFieldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class SpecificationStorageFieldsController implements SpecificationStorageFieldsApi {

  private final SpecificationFieldService specificationFieldService;

  @Override
  public ResponseEntity<Void> deleteField(UUID id) {
    specificationFieldService.deleteField(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<SpecificationFieldDto> updateField(UUID id,
                                                           SpecificationFieldChangeDto specificationFieldChangeDto) {
    return ResponseEntity.accepted().body(specificationFieldService.updateField(id, specificationFieldChangeDto));
  }

  @Override
  public ResponseEntity<FieldIndicatorDtoCollection> getFieldIndicators(UUID fieldId) {
    return ResponseEntity.ok(specificationFieldService.findFieldIndicators(fieldId));
  }

  @Override
  public ResponseEntity<FieldIndicatorDto> createFieldLocalIndicator(UUID fieldId,
                                                                     FieldIndicatorChangeDto createDto) {
    var indicatorDto = specificationFieldService.createLocalIndicator(fieldId, createDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(indicatorDto);
  }
}
