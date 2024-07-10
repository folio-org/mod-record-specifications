package org.folio.rspec.service.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.entity.Specification;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
          MetadataMapper.class,
          FieldMapper.class,
          SpecificationRuleMapper.class,
          StringToFamilyEnumConverter.class,
          StringToFamilyProfileEnumConverter.class
        })
public interface SpecificationMapper {

  @Mapping(target = "fields", ignore = true)
  @Mapping(target = "rules", ignore = true)
  SpecificationDto toDto(Specification specification);

  @Mapping(target = "fields", qualifiedByName = "fieldFullDto")
  @Mapping(target = "rules", source = "specificationRules", qualifiedByName = "ruleFullDto")
  @Mapping(target = "metadata", ignore = true)
  SpecificationDto toFullDto(Specification specification);

  @AfterMapping
  default void resetCollections(@MappingTarget SpecificationDto target) {
    if (CollectionUtils.isEmpty(target.getFields())) {
      target.setFields(null);
    }
    if (CollectionUtils.isEmpty(target.getRules())) {
      target.setRules(null);
    }
  }
}
