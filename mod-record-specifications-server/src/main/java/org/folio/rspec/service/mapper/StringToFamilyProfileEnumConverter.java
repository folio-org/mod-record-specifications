package org.folio.rspec.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFamilyProfileEnumConverter implements Converter<String, FamilyProfile> {

  @Override
  public FamilyProfile convert(@NotNull String source) {
    return StringUtils.isNotBlank(source) ? FamilyProfile.fromValue(source) : null;
  }
}
