package org.folio.rspec.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.MISSING_FIELD_RULE_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@EnablePostgres
@IntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpecificationRuleRepositoryIT {

  @Autowired
  private SpecificationRuleRepository specificationRuleRepository;

  @Test
  void findBySpecificationId() {
    var specificationRuleList = specificationRuleRepository.findBySpecificationId(BIBLIOGRAPHIC_SPECIFICATION_ID);

    assertThat(specificationRuleList)
      .isNotEmpty()
      .hasSize(15);
  }

  @Test
  void updateEnabledBySpecificationRuleId() {
    var specificationRuleId = new SpecificationRuleId(BIBLIOGRAPHIC_SPECIFICATION_ID, MISSING_FIELD_RULE_ID);
    var updated = specificationRuleRepository.updateEnabledBySpecificationRuleId(false, specificationRuleId);

    assertThat(updated).isEqualTo(1);

    var updatedEntity = specificationRuleRepository.findById(specificationRuleId);
    assertThat(updatedEntity)
      .isNotEmpty()
      .hasValueSatisfying(specificationRule -> assertFalse(specificationRule.isEnabled()));
  }
}
