package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.entity.Indicator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class})
public interface FieldIndicatorMapper {

  @Mapping(target = "fieldId", source = "field.id")
  FieldIndicatorDto toDto(Indicator indicator);

  @Mapping(target = "field", ignore = true)
  @Mapping(target = "codes", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  Indicator toEntity(FieldIndicatorChangeDto createDto);

}
