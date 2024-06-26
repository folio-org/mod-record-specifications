package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.entity.Field;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class})
public interface SpecificationFieldMapper {

  @Mapping(target = "specificationId", source = "specification.id")
  SpecificationFieldDto toDto(Field field);

  @Mapping(target = "specification", ignore = true)
  @Mapping(target = "scope", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "url", expression = "java(createDto.getUrl() == null ? null : createDto.getUrl().trim())")
  Field toEntity(SpecificationFieldChangeDto createDto);

  @Mapping(target = "scope", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "specification", ignore = true)
  @Mapping(target = "tag", ignore = true)
  @Mapping(target = "url", expression = "java(changeDto.getUrl() == null ? null : changeDto.getUrl().trim())")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  void update(@MappingTarget Field field, SpecificationFieldChangeDto changeDto);


}
