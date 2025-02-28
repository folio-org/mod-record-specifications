package org.folio.rspec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;

@EnableRetry
@Configuration
public class RetryConfig {

  @Bean("marcSpecificationFetcherRetryTemplate")
  public RetryTemplate retryTemplate() {
    return RetryTemplate.builder()
      .exponentialBackoff(2000, 2, 10000)
      .maxAttempts(5)
      .build();
  }
}
