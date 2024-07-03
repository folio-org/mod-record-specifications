package org.folio.rspec.service.sync;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.rspec.service.SpecificationMetadataService;
import org.folio.rspec.service.sync.fetcher.MarcSpecificationFetcher;
import org.folio.spring.testing.type.UnitTest;
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
    var specId = randomUUID();
    var metadata = prepareMetadata();

    var specification = new Specification();
    specification.setId(specId);

    var fieldsArray = prepareFetchedFields();
    ArgumentCaptor<Collection<Field>> fieldsCaptor = ArgumentCaptor.captor();

    when(metadataService.getSpecificationMetadata(specId)).thenReturn(metadata);
    when(specificationFetcher.fetch(metadata.getSyncUrl())).thenReturn(fieldsArray);
    doNothing().when(specificationFieldService).syncFields(any(), fieldsCaptor.capture());

    specificationSyncService.sync(specification);

    verify(metadataService).saveSpecificationMetadata(metadata);

    assertThat(fieldsCaptor.getValue())
      .extracting(Field::getTag)
      .contains("111", "222");

    assertThat(metadata.getFields()).containsOnlyKeys("111", "222");
  }

  private ArrayNode prepareFetchedFields() {
    var fieldNode = JsonNodeFactory.instance.objectNode();
    fieldNode.put("tag", "222");
    fieldNode.put("label", "label1");
    fieldNode.put("deprecated", false);
    fieldNode.put("repeatable", false);
    fieldNode.put("required", false);
    var fieldsArray = JsonNodeFactory.instance.arrayNode();
    fieldsArray.add(fieldNode);
    return fieldsArray;
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
