package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.entity.SpecificationRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class})
public interface SpecificationRuleMapper {

  @Mapping(target = "id", source = "rule.id")
  @Mapping(target = "code", source = "rule.code")
  @Mapping(target = "name", source = "rule.name")
  @Mapping(target = "specificationId", source = "specification.id")
  @Mapping(target = "description", source = "rule.description")
  @Mapping(target = "metadata", source = "rule.metadata")
  SpecificationRuleDto toDto(SpecificationRule specificationRule);

  @Mapping(target = "id", source = "rule.id")
  @Mapping(target = "code", source = "rule.code")
  @Mapping(target = "name", source = "rule.name")
  @Mapping(target = "description", source = "rule.description")
  @Named("ruleFulDto")
  SpecificationRuleDto toFullDto(SpecificationRule specificationRule);

}
