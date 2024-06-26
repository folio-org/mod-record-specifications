package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class})
public interface IndicatorCodeMapper {

  @Mapping(target = "indicatorId", source = "indicator.id")
  IndicatorCodeDto toDto(IndicatorCode code);

  @Mapping(target = "scope", ignore = true)
  @Mapping(target = "indicator", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  IndicatorCode toEntity(IndicatorCodeChangeDto createDto);

}
