package org.folio.rspec.i18n;

@FunctionalInterface
public interface TranslationProvider {

  String format(String key, Object... args);
}
