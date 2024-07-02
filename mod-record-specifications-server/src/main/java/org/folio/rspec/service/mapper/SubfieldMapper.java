package org.folio.rspec.service.mapper;

import org.folio.rspec.domain.dto.FieldSubfieldChangeDto;
import org.folio.rspec.domain.dto.FieldSubfieldDto;
import org.folio.rspec.domain.entity.Subfield;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MetadataMapper.class})
public interface SubfieldMapper {

  @Mapping(target = "fieldId", source = "field.id")
  FieldSubfieldDto toDto(Subfield subfield);

  @Mapping(target = "scope", ignore = true)
  @Mapping(target = "field", ignore = true)
  @Mapping(target = "metadata", ignore = true)
  @Mapping(target = "id", ignore = true)
  Subfield toEntity(FieldSubfieldChangeDto createDto);

}
