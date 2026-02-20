package org.folio.rspec.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;

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
}
