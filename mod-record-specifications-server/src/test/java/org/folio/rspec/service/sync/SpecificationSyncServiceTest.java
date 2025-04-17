package org.folio.rspec.service.sync;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
}
