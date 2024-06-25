package org.folio.rspec.config;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

  @Bean
  public ValidationConfigurationCustomizer customizer() {
    return configuration -> {
      if (configuration instanceof HibernateValidatorConfiguration config) {
        var constraintMapping = config.createConstraintMapping();
        constraintMapping
          .constraintDefinition(URL.class)
          .includeExistingValidators(false)
          .validatedBy(RegexpUrlValidator.class);
        config.addMapping(constraintMapping);
      }
    };
  }
}
