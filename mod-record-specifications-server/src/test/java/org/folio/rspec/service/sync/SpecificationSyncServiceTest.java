package org.folio.rspec.service.sync;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.rspec.service.SpecificationMetadataService;
import org.folio.rspec.service.sync.fetcher.MarcSpecificationFetcher;
import org.folio.spring.testing.type.UnitTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SpecificationSyncServiceTest {

  @Mock
  private SpecificationMetadataService metadataService;
  @Mock
  private MarcSpecificationFetcher specificationFetcher;
  @Mock
  private SpecificationFieldService specificationFieldService;

  @InjectMocks
  private SpecificationSyncService specificationSyncService;

  @Test
  void sync_fetchesAndSyncsFields() {
    final var specId = randomUUID();
    final var metadata = prepareMetadata();

    var specification = new Specification();
    specification.setId(specId);
    specification.setFamily(Family.MARC);
    specification.setProfile(FamilyProfile.AUTHORITY);

    var fieldsArray = prepareFetchedFields();
    ArgumentCaptor<Collection<Field>> fieldsCaptor = ArgumentCaptor.captor();

    when(metadataService.getSpecificationMetadata(specId)).thenReturn(metadata);
    when(specificationFetcher.fetch(Family.MARC, FamilyProfile.AUTHORITY)).thenReturn(fieldsArray);
    doNothing().when(specificationFieldService).syncFields(any(), fieldsCaptor.capture());

    specificationSyncService.sync(specification);

    verify(metadataService).saveSpecificationMetadata(metadata);

    assertThat(fieldsCaptor.getValue())
      .extracting(Field::getTag, Field::getUrl)
      .contains(tuple("111", "url"), tuple("222", "format"), tuple("333", null));

    assertThat(metadata.getFields()).containsOnlyKeys("111", "222", "333");
  }

  @Test
  @SuppressWarnings("checkstyle:methodLength")
  void sync_shouldPreferNonObsoleteEntitiesOverObsoleteOnes() {
    final var specId = randomUUID();
    final var metadata = prepareMetadata();

    var specification = new Specification();
    specification.setId(specId);
    specification.setFamily(Family.MARC);
    specification.setProfile(FamilyProfile.BIBLIOGRAPHIC);

    var fieldsArray = prepareFieldsWithDuplicates();
    ArgumentCaptor<Collection<Field>> fieldsCaptor = ArgumentCaptor.captor();

    when(metadataService.getSpecificationMetadata(specId)).thenReturn(metadata);
    when(specificationFetcher.fetch(Family.MARC, FamilyProfile.BIBLIOGRAPHIC)).thenReturn(fieldsArray);
    doNothing().when(specificationFieldService).syncFields(any(), fieldsCaptor.capture());

    specificationSyncService.sync(specification);

    var syncedFields = fieldsCaptor.getValue();

    // Verify duplicate fields - should keep non-obsolete field
    var fields856 = syncedFields.stream()
      .filter(field -> "856".equals(field.getTag()))
      .toList();

    assertThat(fields856).hasSize(1);
    var field856 = fields856.getFirst();
    assertThat(field856.isDeprecated()).isFalse();
    assertThat(field856.getLabel()).isEqualTo("Electronic Location and Access (Non-obsolete)");

    // Verify duplicate subfields - should keep non-obsolete subfield
    var subfieldsH = field856.getSubfields().stream()
      .filter(subfield -> "h".equals(subfield.getCode()))
      .toList();

    assertThat(subfieldsH).hasSize(1);
    var subfieldH = subfieldsH.getFirst();
    assertThat(subfieldH.isDeprecated()).isFalse();
    assertThat(subfieldH.getLabel()).isEqualTo("Non-functioning Uniform Resource Identifier");

    // Verify duplicate indicator codes - should keep non-obsolete indicator code
    var firstIndicator = field856.getIndicators().stream()
      .filter(indicator -> 1 == indicator.getOrder())
      .findFirst()
      .orElseThrow(() -> new AssertionError("First indicator not found"));

    var indicatorCodes0 = firstIndicator.getCodes().stream()
      .filter(code -> "0".equals(code.getCode()))
      .toList();

    assertThat(indicatorCodes0).hasSize(1);
    var indicatorCode0 = indicatorCodes0.getFirst();
    assertThat(indicatorCode0.isDeprecated()).isFalse();
    assertThat(indicatorCode0.getLabel()).isEqualTo("Email (Non-obsolete)");
  }

  private ArrayNode prepareFetchedFields() {
    var fieldNode1 = prepareFieldNode("222", "label1", false, true, false);
    var fieldNode2 = prepareFieldNode("333", "label2", true, false, true);
    var fieldsArray = JsonNodeFactory.instance.arrayNode();
    fieldsArray.add(fieldNode1);
    fieldsArray.add(fieldNode2);
    return fieldsArray;
  }

  private @NotNull ObjectNode prepareFieldNode(String number, String label1, boolean deprecated, boolean repeatable,
                                               boolean required) {
    var fieldNode1 = JsonNodeFactory.instance.objectNode();
    fieldNode1.put("tag", number);
    fieldNode1.put("label", label1);
    fieldNode1.put("deprecated", deprecated);
    fieldNode1.put("repeatable", repeatable);
    fieldNode1.put("required", required);
    return fieldNode1;
  }

  private SpecificationMetadata prepareMetadata() {
    Map<String, FieldMetadata> fieldsMetadata = new HashMap<>();
    fieldsMetadata.put("111", new FieldMetadata(randomUUID().toString(), "111", Scope.SYSTEM.name(),
      true, "label", true, false, true, "url", null, null));
    var metadata = new SpecificationMetadata();
    metadata.setFields(fieldsMetadata);
    metadata.setUrlFormat("format");
    return metadata;
  }

  private ArrayNode prepareFieldsWithDuplicates() {
    var field856 = prepareFieldNode("856", "Electronic Location and Access (Non-obsolete)", false, true, false);
    var subfieldsArray = JsonNodeFactory.instance.arrayNode();

    // Add duplicate subfields: obsolete first, non-obsolete second
    subfieldsArray.add(prepareSubfieldNode("Processor of request (NR) [OBSOLETE]", true, false));
    subfieldsArray.add(prepareSubfieldNode("Non-functioning Uniform Resource Identifier", false, true));
    field856.set("subfields", subfieldsArray);

    var indicatorNode1 = JsonNodeFactory.instance.objectNode();
    indicatorNode1.put("order", 1);
    indicatorNode1.put("label", "Access method");

    var indicatorCodesArray = JsonNodeFactory.instance.arrayNode();
    // Add duplicate indicator codes: obsolete first, non-obsolete second
    indicatorCodesArray.add(prepareIndicatorCodeNode("Email (Obsolete)", true));
    indicatorCodesArray.add(prepareIndicatorCodeNode("Email (Non-obsolete)", false));
    indicatorNode1.set("codes", indicatorCodesArray);

    var indicatorsArray = JsonNodeFactory.instance.arrayNode();
    indicatorsArray.add(indicatorNode1);
    field856.set("indicators", indicatorsArray);

    var fieldsArray = JsonNodeFactory.instance.arrayNode();
    var obsoleteField856 = prepareFieldNode("856", "Electronic Location and Access (Obsolete)", true, true, false);
    fieldsArray.add(obsoleteField856);
    fieldsArray.add(field856);
    return fieldsArray;
  }

  private ObjectNode prepareSubfieldNode(String label, boolean deprecated,
                                         boolean repeatable) {
    var subfieldNode = JsonNodeFactory.instance.objectNode();
    subfieldNode.put("code", "h");
    subfieldNode.put("label", label);
    subfieldNode.put("deprecated", deprecated);
    subfieldNode.put("repeatable", repeatable);
    subfieldNode.put("required", false);
    return subfieldNode;
  }

  private ObjectNode prepareIndicatorCodeNode(String label, boolean deprecated) {
    var codeNode = JsonNodeFactory.instance.objectNode();
    codeNode.put("code", "0");
    codeNode.put("label", label);
    codeNode.put("deprecated", deprecated);
    return codeNode;
  }
}
