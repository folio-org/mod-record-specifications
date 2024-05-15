package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFullDto;
import org.folio.rspec.domain.entity.Specification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
          MetadataMapper.class,
          SpecificationRuleMapper.class,
          StringToFamilyEnumConverter.class,
          StringToFamilyProfileEnumConverter.class
        })
public interface SpecificationMapper {

  SpecificationDto toDto(Specification specification);

  @Mapping(target = "fields", ignore = true)
  @Mapping(target = "rules", source = "specificationRules")
  SpecificationFullDto toFullDto(Specification specification);

}
