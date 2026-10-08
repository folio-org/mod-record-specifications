package org.folio.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldSubfieldsPath;
import static org.folio.support.ApiEndpoints.specificationSyncPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.QueryParams;
import org.folio.support.SpecificationFieldTreeHelper;
import org.folio.support.SpecificationITBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Covers the {@code preserveLocal} sync mode: local field/indicator/subfield/indicator-code
 * definitions a cataloger created should survive a sync unless the spec now defines the exact
 * same tag/order/code itself, in which case the spec definition must win. Without the flag (the
 * default), sync keeps its original wipe-and-recreate behavior.
 */
@IntegrationTest
@DatabaseCleanup(tables = FIELD_TABLE_NAME, tenants = TENANT_ID)
class SpecificationStoragePreserveLocalSyncApiIT extends SpecificationITBase {

  private static final QueryParams PRESERVE_LOCAL = new QueryParams().addQueryParam("preserveLocal", "true");

  @Autowired
  private FieldRepository fieldRepository;
  @Autowired
  private IndicatorRepository indicatorRepository;
  @Autowired
  private IndicatorCodeRepository indicatorCodeRepository;
  @Autowired
  private SubfieldRepository subfieldRepository;

  private SpecificationFieldTreeHelper fieldTree;

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @BeforeEach
  void setUp() throws Exception {
    fieldTree = new SpecificationFieldTreeHelper(fieldRepository, indicatorRepository, indicatorCodeRepository,
      subfieldRepository);
    // establish a clean, fully-synced baseline before every test
    doPost(specificationSyncPath(BIBLIOGRAPHIC_SPECIFICATION_ID), null)
      .andExpect(status().isAccepted());
  }

  @Test
  void syncWithPreserveLocal_keepsLocalFieldAndLocalAdditionsOnStandardFields() throws Exception {
    // a whole field the spec knows nothing about, with its own local indicator and code
    // (indicator order is capped at 1-2 by the schema, same as every real MARC field)
    var localFieldId = createLocalFieldId("950");
    var localIndicatorId = createLocalIndicatorId(localFieldId, localTestIndicator(1));
    var localCodeId = createLocalCodeId(localIndicatorId, localTestCode("z"));

    // a local subfield added on top of an otherwise-STANDARD field; $9 isn't part of the real
    // MARC 100 subfields, so it's free for a cataloger to add locally
    var field100Id = executeInContext(() -> fieldTree.findFieldOrFail(BIBLIOGRAPHIC_SPECIFICATION_ID, "100").getId());
    doPost(fieldSubfieldsPath(field100Id), localTestSubfield("9", "Linked authority UUID"))
      .andExpect(status().isCreated());

    doPost(specificationSyncPath(BIBLIOGRAPHIC_SPECIFICATION_ID, PRESERVE_LOCAL), null)
      .andExpect(status().isAccepted());

    executeInContext(() -> {
      assertThat(fieldTree.findField(BIBLIOGRAPHIC_SPECIFICATION_ID, "950")).isPresent();
      assertThat(fieldRepository.findById(localFieldId)).as("local field kept its id").isPresent();
      assertThat(indicatorRepository.findById(localIndicatorId)).as("local indicator kept its id").isPresent();
      assertThat(indicatorCodeRepository.findById(localCodeId)).as("local indicator code kept its id").isPresent();

      var refreshedField100 = fieldTree.findFieldOrFail(BIBLIOGRAPHIC_SPECIFICATION_ID, "100");
      var subfield9 = fieldTree.findSubfieldOrFail(refreshedField100.getId(), "9");
      assertThat(subfield9.getScope()).isEqualTo(Scope.LOCAL);
      return null;
    });
  }

  @Test
  void syncWithPreserveLocal_overridesLocalRowWhenSpecNowDefinesSameKey() throws Exception {
    // simulate a cataloger having locally customized subfield $h on field 341 before the spec
    // defined it; MRSPECS-212 added a real $h ("Sensory hazards") to field 341 since then.
    var field341Id = executeInContext(() -> fieldTree.findFieldOrFail(BIBLIOGRAPHIC_SPECIFICATION_ID, "341").getId());
    executeInContext(() -> {
      var subfieldH = fieldTree.findSubfieldOrFail(field341Id, "h");
      subfieldH.setScope(Scope.LOCAL);
      subfieldH.setLabel("Bogus local label");
      subfieldRepository.save(subfieldH);
      return null;
    });

    doPost(specificationSyncPath(BIBLIOGRAPHIC_SPECIFICATION_ID, PRESERVE_LOCAL), null)
      .andExpect(status().isAccepted());

    executeInContext(() -> {
      var refreshedField341 = fieldTree.findFieldOrFail(BIBLIOGRAPHIC_SPECIFICATION_ID, "341");
      var subfieldH = fieldTree.findSubfieldOrFail(refreshedField341.getId(), "h");
      assertThat(subfieldH.getScope()).as("spec definition overrides the stale local one").isEqualTo(Scope.STANDARD);
      assertThat(subfieldH.getLabel()).isEqualTo("Sensory hazards");
      return null;
    });
  }

  @Test
  void syncWithoutPreserveLocal_stillWipesLocalField() throws Exception {
    createLocalFieldId("950");

    doPost(specificationSyncPath(BIBLIOGRAPHIC_SPECIFICATION_ID), null)
      .andExpect(status().isAccepted());

    executeInContext(() -> {
      assertThat(fieldTree.findField(BIBLIOGRAPHIC_SPECIFICATION_ID, "950")).isEmpty();
      return null;
    });
  }

  private UUID createLocalFieldId(String tag) throws Exception {
    return UUID.fromString(createLocalField(tag));
  }

  private UUID createLocalIndicatorId(UUID fieldId, FieldIndicatorChangeDto dto) {
    return UUID.fromString(createLocalIndicator(fieldId.toString(), dto));
  }

  private UUID createLocalCodeId(UUID indicatorId, IndicatorCodeChangeDto dto) {
    return UUID.fromString(createLocalCode(indicatorId.toString(), dto));
  }
}
