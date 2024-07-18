package org.folio.rspec.i18n;

public class TranslationProviderDelegate implements TranslationProvider {

  private static final String MESSAGE_KEY_PREFIX = "mod-record-specifications.validation.";

  private final TranslationProvider delegate;

  public TranslationProviderDelegate(TranslationProvider delegate) {
    this.delegate = delegate;
  }

  @Override
  public String format(String key, Object... args) {
    return delegate.format(MESSAGE_KEY_PREFIX + key, args);
  }
}
