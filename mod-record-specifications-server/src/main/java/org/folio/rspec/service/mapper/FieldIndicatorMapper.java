package org.folio.rspec.service.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.entity.Indicator;
import org.mapstruct.BeanMapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
          MetadataMapper.class,
          IndicatorCodeMapper.class
        })
public interface FieldIndicatorMapper {

  @Mapping(target = "fieldId", source = "field.id")
  @Mapping(target = "codes", ignore = true)
  FieldIndicatorDto toDto(Indicator indicator);

  @Mapping(target = "fieldId", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "codes", qualifiedByName = "indicatorCodeFullDto")
  @Named("indicatorFullDto")
  FieldIndicatorDto toFullDto(Indicator indicator);

  @Mapping(target = "field", ignore = true)
  @Mapping(target = "codes", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  Indicator toEntity(FieldIndicatorChangeDto createDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "field", ignore = true)
  @Mapping(target = "codes", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void update(@MappingTarget Indicator indicator, FieldIndicatorChangeDto changeDto);

  @AfterMapping
  default void resetCollections(@MappingTarget FieldIndicatorDto target) {
    if (CollectionUtils.isEmpty(target.getCodes())) {
      target.setCodes(null);
    }
  }
}
