package org.folio.rspec.config;

import jakarta.validation.constraints.NotNull;
import org.folio.rspec.service.validation.constraint.NotNullValidator;
import org.folio.rspec.service.validation.constraint.RegexpUrlValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
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
        var urlConstraintMapping = getUrlConstraintMapping(config);
        var requiredConstraintMapping = getRequiredConstraintMapping(config);
        config.addMapping(urlConstraintMapping);
        config.addMapping(requiredConstraintMapping);
      }
    };
  }

  private ConstraintMapping getRequiredConstraintMapping(HibernateValidatorConfiguration config) {
    var constraintMapping = config.createConstraintMapping();
    constraintMapping
      .constraintDefinition(NotNull.class)
      .includeExistingValidators(false)
      .validatedBy(NotNullValidator.class);
    return constraintMapping;
  }

  private ConstraintMapping getUrlConstraintMapping(HibernateValidatorConfiguration config) {
    var constraintMapping = config.createConstraintMapping();
    constraintMapping
      .constraintDefinition(URL.class)
      .includeExistingValidators(false)
      .validatedBy(RegexpUrlValidator.class);
    return constraintMapping;
  }
}
