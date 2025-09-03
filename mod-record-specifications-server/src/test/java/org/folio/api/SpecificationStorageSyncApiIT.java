package org.folio.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.support.ApiEndpoints.specificationSyncPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.support.UuidPersistable;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.SpecificationITBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DatabaseCleanup(tables = FIELD_TABLE_NAME, tenants = TENANT_ID)
class SpecificationStorageSyncApiIT extends SpecificationITBase {

  @Autowired
  private FieldRepository fieldRepository;
  @Autowired
  private SubfieldRepository subfieldRepository;
  @Autowired
  private IndicatorRepository indicatorRepository;
  @Autowired
  private IndicatorCodeRepository indicatorCodeRepository;

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void syncSpecification_produceSameEntitiesEachTime() throws Exception {
    var specificationId = BIBLIOGRAPHIC_SPECIFICATION_ID;

    doPost(specificationSyncPath(specificationId), null);

    var createdFieldIds = executeInContext(() -> toIdArray(fieldRepository.findAll()));
    var createdSubfieldIds = executeInContext(() -> toIdArray(subfieldRepository.findAll()));
    var createdIndicatorIds = executeInContext(() -> toIdArray(indicatorRepository.findAll()));
    var createdIndicatorCodeIds = executeInContext(() -> toIdArray(indicatorCodeRepository.findAll()));

    // check if second sync will produce same results
    doPost(specificationSyncPath(specificationId), null)
      .andExpect(status().isAccepted());

    var recreatedFields = executeInContext(() -> fieldRepository.findAll());
    var recreatedSubfields = executeInContext(() -> subfieldRepository.findAll());
    var recreatedIndicator = executeInContext(() -> indicatorRepository.findAll());
    var recreatedIndicatorCodes = executeInContext(() -> indicatorCodeRepository.findAll());

    assertThat(recreatedFields)
      .hasSize(293)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdFieldIds);

    assertThat(recreatedSubfields)
      .hasSize(2840)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdSubfieldIds);

    assertThat(recreatedIndicator)
      .hasSize(528)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdIndicatorIds);

    assertThat(recreatedIndicatorCodes)
      .hasSize(1193)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdIndicatorCodeIds);

    assertSpecificationUpdatedEvent();
  }

  private UUID @NotNull [] toIdArray(List<? extends UuidPersistable> all) {
    return all.stream().map(UuidPersistable::getId).toArray(UUID[]::new);
  }
}
