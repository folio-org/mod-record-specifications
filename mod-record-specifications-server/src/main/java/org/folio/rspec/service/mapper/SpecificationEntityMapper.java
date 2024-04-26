package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.entity.Specification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class, StringToFamilyEnumConverter.class, StringToFamilyProfileEnumConverter.class})
public interface SpecificationEntityMapper {

  @Mapping(target = "metadata", ignore = true)
  Specification toEntity(SpecificationDto specificationFull);

  SpecificationDto toDto(Specification specification);

}
