package org.folio.rspec.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDtoCollection;
import org.folio.rspec.rest.resource.SpecificationStorageIndicatorsApi;
import org.folio.rspec.service.FieldIndicatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class SpecificationStorageIndicatorsController implements SpecificationStorageIndicatorsApi {

  private final FieldIndicatorService fieldIndicatorService;

  @Override
  public ResponseEntity<IndicatorCodeDtoCollection> getIndicatorCodes(UUID indicatorId) {
    return ResponseEntity.ok(fieldIndicatorService.findIndicatorCodes(indicatorId));
  }

  @Override
  public ResponseEntity<IndicatorCodeDto> createIndicatorLocalCode(UUID indicatorId,
                                                                   IndicatorCodeChangeDto createDto) {
    var codeDto = fieldIndicatorService.createLocalCode(indicatorId, createDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(codeDto);
  }
}
