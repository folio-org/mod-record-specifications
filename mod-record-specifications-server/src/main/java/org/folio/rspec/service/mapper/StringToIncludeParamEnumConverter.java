package org.folio.rspec.service.mapper;

import org.apache.commons.lang3.StringUtils;
import org.folio.rspec.domain.dto.IncludeParam;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToIncludeParamEnumConverter implements Converter<String, IncludeParam> {

  @Override
  public IncludeParam convert(@NotNull String source) {
    return StringUtils.isNotBlank(source) ? IncludeParam.fromValue(source) : null;
  }
}
