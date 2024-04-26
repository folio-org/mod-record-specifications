package org.folio.rspec.service.mapper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.folio.rspec.domain.dto.MetadataDto;
import org.folio.rspec.domain.entity.support.Metadata;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MetadataMapper {

  SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  MetadataDto toDto(Metadata source);

  default String map(Timestamp timestamp) {
    return timestamp != null ? DATE_TIME_FORMAT.format(timestamp) : null;
  }

}
