package org.folio.rspec.service;

import org.folio.spring.i18n.config.TranslationConfiguration;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

@Primary
@Service
public class ModuleTranslationService extends TranslationService {

  private static final String MODULE_KEY_PREFIX = "mod-record-specifications.";

  public ModuleTranslationService(ResourcePatternResolver resourceResolver,
                                  TranslationConfiguration configuration) {
    super(resourceResolver, configuration);
  }

  @Override
  public String format(String key, Object... args) {
    return super.format(MODULE_KEY_PREFIX + key, args);
  }
}
