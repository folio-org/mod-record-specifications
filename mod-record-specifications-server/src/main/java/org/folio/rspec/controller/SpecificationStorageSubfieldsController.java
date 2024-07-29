package org.folio.rspec.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.rest.resource.SpecificationStorageSubfieldsApi;
import org.folio.rspec.service.SubfieldService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class SpecificationStorageSubfieldsController implements SpecificationStorageSubfieldsApi {

  private final SubfieldService subfieldService;

  @Override
  public ResponseEntity<Void> deleteSubfield(UUID subfieldId) {
    subfieldService.deleteSubfield(subfieldId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<SubfieldDto> updateSubfield(UUID subfieldId, SubfieldChangeDto subfieldChangeDto) {
    return ResponseEntity.accepted().body(subfieldService.updateSubfield(subfieldId, subfieldChangeDto));
  }
}
