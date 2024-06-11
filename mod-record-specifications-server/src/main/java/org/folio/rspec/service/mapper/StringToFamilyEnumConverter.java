package org.folio.rspec.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.folio.rspec.domain.dto.Family;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFamilyEnumConverter implements Converter<String, Family> {

  @Override
  public Family convert(String source) {
    return StringUtils.isNotBlank(source) ? Family.fromValue(source) : null;
  }
}
