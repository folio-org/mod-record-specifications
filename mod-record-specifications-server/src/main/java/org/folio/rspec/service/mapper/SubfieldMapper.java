package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.entity.Subfield;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class})
public interface SubfieldMapper {

  @Mapping(target = "fieldId", source = "field.id")
  SubfieldDto toDto(Subfield subfield);

  @Mapping(target = "fieldId", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Named("subfieldFullDto")
  SubfieldDto toFullDto(Subfield subfield);

  @Mapping(target = "scope", ignore = true)
  @Mapping(target = "field", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  Subfield toEntity(SubfieldChangeDto createDto);

  @Mapping(target = "field", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  Subfield toEntity(SubfieldDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "scope", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "field", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void update(@MappingTarget Subfield subfield, SubfieldChangeDto changeDto);
}
