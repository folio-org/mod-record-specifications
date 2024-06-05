package org.folio.rspec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

@Configuration
public class TranslationConfig {

  @Bean
  public MessageCodesResolver messageCodesResolver() {
    return new DefaultMessageCodesResolver();
  }
}
