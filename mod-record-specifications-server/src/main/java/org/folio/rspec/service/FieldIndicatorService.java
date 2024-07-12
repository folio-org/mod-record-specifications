package org.folio.rspec.service;

import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDtoCollection;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.service.mapper.FieldIndicatorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class FieldIndicatorService {

  private final IndicatorRepository repository;
  private final FieldIndicatorMapper mapper;
  private final IndicatorCodeService codeService;

  public FieldIndicatorDtoCollection findFieldIndicators(UUID fieldId) {
    log.debug("findFieldIndicators::fieldId={}", fieldId);
    var indicatorDtos = repository.findByFieldId(fieldId).stream()
      .map(mapper::toDto)
      .toList();
    return new FieldIndicatorDtoCollection()
      .indicators(indicatorDtos)
      .totalRecords(indicatorDtos.size());
  }

  public FieldIndicatorDto createLocalIndicator(Field field, FieldIndicatorChangeDto createDto) {
    log.info("createLocalIndicator::fieldId={}, dto={}", field.getId(), createDto);
    var indicatorEntity = mapper.toEntity(createDto);
    indicatorEntity.setField(field);
    return mapper.toDto(repository.save(indicatorEntity));
  }

  @Transactional
  public IndicatorCodeDtoCollection findIndicatorCodes(UUID indicatorId) {
    log.debug("findIndicatorCodes::indicatorId={}", indicatorId);
    return doForIndicatorOrFail(indicatorId,
      indicator -> codeService.findIndicatorCodes(indicatorId)
    );
  }

  @Transactional
  public IndicatorCodeDto createLocalCode(UUID indicatorId, IndicatorCodeChangeDto createDto) {
    log.debug("createLocalCode::indicatorId={}, createDto={}", indicatorId, createDto);
    return doForIndicatorOrFail(indicatorId,
      indicator -> codeService.createLocalCode(indicator, createDto)
    );
  }

  @Transactional
  public FieldIndicatorDto updateIndicator(UUID id, FieldIndicatorChangeDto changeDto) {
    log.info("updateIndicator::id={}, dto={}", id, changeDto);
    var indicatorEntity = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forIndicator(id));
    var field = indicatorEntity.getField();
    if (field.getScope() != Scope.LOCAL) {
      throw ScopeModificationNotAllowedException.forUpdate(field.getScope(), Indicator.INDICATOR_TABLE_NAME);
    }
    mapper.update(indicatorEntity, changeDto);

    return mapper.toDto(repository.save(indicatorEntity));
  }

  private <T> T doForIndicatorOrFail(UUID indicatorId, Function<Indicator, T> action) {
    return repository.findById(indicatorId)
      .map(action)
      .orElseThrow(() -> ResourceNotFoundException.forIndicator(indicatorId));
  }
}
