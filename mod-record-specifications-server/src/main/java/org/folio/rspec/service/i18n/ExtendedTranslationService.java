package org.folio.rspec.service.i18n;

import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.spring.i18n.config.TranslationConfiguration;
import org.folio.spring.i18n.service.TranslationService;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

@Primary
@Service
public class ExtendedTranslationService extends TranslationService {

  private static final String MODULE_KEY_PREFIX = "mod-record-specifications.";
  private static final String ERROR_MESSAGE_MSG_ARG = "errorMessage";

  public ExtendedTranslationService(ResourcePatternResolver resourceResolver,
                                    TranslationConfiguration configuration) {
    super(resourceResolver, configuration);
  }

  @Override
  public String format(String key, Object... args) {
    return super.format(MODULE_KEY_PREFIX + key, args);
  }

  public String formatUnexpected(String message) {
    return format(ErrorCode.UNEXPECTED.getMessageKey(), ERROR_MESSAGE_MSG_ARG, message);
  }
}
