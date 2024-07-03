package org.folio.rspec.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDtoCollection;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.service.mapper.IndicatorCodeMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class IndicatorCodeService {

  private final IndicatorCodeRepository repository;
  private final IndicatorCodeMapper mapper;

  public IndicatorCodeDtoCollection findIndicatorCodes(UUID indicatorId) {
    log.debug("findIndicatorCodes::indicatorId={}", indicatorId);
    var indicatorCodeDtos = repository.findByIndicatorId(indicatorId).stream()
      .map(mapper::toDto)
      .toList();
    return new IndicatorCodeDtoCollection()
      .codes(indicatorCodeDtos)
      .totalRecords(indicatorCodeDtos.size());
  }

  public IndicatorCodeDto createLocalCode(Indicator indicator, IndicatorCodeChangeDto createDto) {
    log.info("createLocalCode::indicatorId={}, dto={}", indicator.getId(), createDto);
    var codeEntity = mapper.toEntity(createDto);
    codeEntity.setIndicator(indicator);
    codeEntity.setScope(Scope.LOCAL);
    return mapper.toDto(repository.save(codeEntity));
  }
}
