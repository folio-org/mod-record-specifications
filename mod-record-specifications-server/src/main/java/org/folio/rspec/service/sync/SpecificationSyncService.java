package org.folio.rspec.service.sync;

import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.DEPRECATED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.LABEL_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REPEATABLE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REQUIRED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.TAG_PROP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.rspec.service.SpecificationMetadataService;
import org.folio.rspec.service.sync.fetcher.MarcSpecificationFetcher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecificationSyncService {

  private final SpecificationMetadataService metadataService;
  private final MarcSpecificationFetcher specificationFetcher;
  private final SpecificationFieldService specificationFieldService;

  public void sync(Specification specification) {
    var specificationMetadata = metadataService.getSpecificationMetadata(specification.getId());
    var fieldsMetadata = specificationMetadata.getFields();
    var specificationFields = specificationFetcher.fetch(specificationMetadata.getSyncUrl());
    if (specificationFields != null && !specificationFields.isEmpty()) {
      syncFields(specification, specificationFields, fieldsMetadata, specificationMetadata);
      metadataService.saveSpecificationMetadata(specificationMetadata);
    }
  }

  private void syncFields(Specification specification, ArrayNode specificationFields,
                          Map<String, FieldMetadata> fieldsMetadata, SpecificationMetadata specificationMetadata) {
    List<Field> fields = new ArrayList<>();
    for (var fieldElement : specificationFields) {
      var tag = getText(fieldElement, TAG_PROP);
      var fieldMetadata = specificationMetadata.getFields()
        .computeIfAbsent(tag, definedTag -> new FieldMetadata(definedTag, Scope.STANDARD.name()));
      var field = toField(fieldElement, fieldMetadata, specificationMetadata.getUrlFormat());
      fields.add(field);
    }
    for (FieldMetadata value : fieldsMetadata.values()) {
      if (Boolean.TRUE.equals(value.defaultValue())) {
        var defaultField = toField(value);
        fields.add(defaultField);
      }
    }
    specificationFieldService.syncFields(specification.getId(), fields);
  }

  private Field toField(FieldMetadata value) {
    var defaultField = new Field();
    defaultField.setId(UUID.fromString(value.id()));
    defaultField.setTag(value.tag());
    defaultField.setUrl(value.url());
    defaultField.setScope(Scope.valueOf(value.scope()));
    defaultField.setLabel(value.label());
    defaultField.setDeprecated(value.deprecated());
    defaultField.setRepeatable(value.repeatable());
    defaultField.setRequired(value.required());
    return defaultField;
  }

  private Field toField(JsonNode fieldElement, FieldMetadata fieldMetadata, String fieldUrlFormat) {
    var field = new Field();
    field.setId(UUID.fromString(fieldMetadata.id()));
    field.setTag(fieldMetadata.tag());
    field.setUrl(fieldUrlFormat.formatted(field.getTag()));
    field.setScope(Scope.valueOf(fieldMetadata.scope()));
    field.setLabel(getText(fieldElement, LABEL_PROP));
    field.setDeprecated(getBoolean(fieldElement, DEPRECATED_PROP));
    field.setRepeatable(getBoolean(fieldElement, REPEATABLE_PROP));
    field.setRequired(isRequired(fieldElement, fieldMetadata));
    return field;
  }

  private boolean isRequired(JsonNode fieldElement, FieldMetadata fieldMetadata) {
    return fieldMetadata.required() != null ? fieldMetadata.required() : getBoolean(fieldElement, REQUIRED_PROP);
  }

  private String getText(JsonNode fieldElement, String fieldName) {
    return fieldElement.get(fieldName).asText();
  }

  private boolean getBoolean(JsonNode fieldElement, String fieldName) {
    return fieldElement.get(fieldName).asBoolean();
  }
}
