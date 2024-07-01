package org.folio.rspec.service.sync;

import static org.folio.rspec.JsonUtils.getBoolean;
import static org.folio.rspec.JsonUtils.getInt;
import static org.folio.rspec.JsonUtils.getText;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.CODES_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.CODE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.DEPRECATED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.INDICATORS_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.LABEL_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.ORDER_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REPEATABLE_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.REQUIRED_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.SUBFIELDS_PROP;
import static org.folio.rspec.service.sync.fetcher.MarcSpecificationConstants.TAG_PROP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.domain.entity.metadata.IndicatorCodeMetadata;
import org.folio.rspec.domain.entity.metadata.IndicatorMetadata;
import org.folio.rspec.domain.entity.metadata.SubfieldMetadata;
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

    Map<String, Field> fieldByTags = new HashMap<>();
    for (Field field : fields) {
      fieldByTags.merge(field.getTag(), field, (field1, field2) -> field1.isDeprecated() ? field2 : field1);
      field.setSpecification(specification);
      Map<String, Subfield> subfields = new HashMap<>();
      for (Subfield subfield : field.getSubfields()) {
        subfields.merge(subfield.getCode(), subfield,
          (subfield1, subfield2) -> subfield1.isDeprecated() ? subfield2 : subfield1);
      }
      field.setSubfields(new HashSet<>(subfields.values()));
      for (Indicator indicator : field.getIndicators()) {
        Map<String, IndicatorCode> indicatorCodes = new HashMap<>();
        for (IndicatorCode indicatorCode : indicator.getCodes()) {
          indicatorCodes.merge(indicatorCode.getCode(), indicatorCode,
            (indicatorCode1, indicatorCode2) -> indicatorCode1.isDeprecated() ? indicatorCode2 : indicatorCode1);
        }
        indicator.setCodes(new ArrayList<>(indicatorCodes.values()));
      }
    }
    specificationFieldService.syncFields(specification, fieldByTags.values());
  }

  private Field toField(FieldMetadata fieldMetadata) {
    var defaultField = new Field();
    defaultField.setId(UUID.fromString(fieldMetadata.id()));
    defaultField.setTag(fieldMetadata.tag());
    defaultField.setUrl(fieldMetadata.url());
    defaultField.setScope(Scope.valueOf(fieldMetadata.scope()));
    defaultField.setLabel(fieldMetadata.label());
    defaultField.setDeprecated(fieldMetadata.deprecated());
    defaultField.setRepeatable(fieldMetadata.repeatable());
    defaultField.setRequired(fieldMetadata.required());

    Set<Subfield> subfields = new HashSet<>();
    if (fieldMetadata.subfields() != null) {
      for (SubfieldMetadata subfieldMetadata : fieldMetadata.subfields().values()) {
        if (subfieldMetadata.defaultValue()) {
          subfields.add(toSubfield(subfieldMetadata));
        }
      }
      defaultField.setSubfields(subfields);
    }

    if (fieldMetadata.indicators() != null) {
      List<Indicator> indicators = new ArrayList<>();
      for (IndicatorMetadata indicatorMetadata : fieldMetadata.indicators().values()) {
        if (indicatorMetadata.defaultValue()) {
          var indicator = new Indicator();
          indicator.setId(UUID.fromString(indicatorMetadata.id()));
          indicator.setOrder(indicatorMetadata.order());
          indicator.setLabel(indicatorMetadata.label());
          List<IndicatorCode> indicatorCodes = new ArrayList<>();
          for (IndicatorCodeMetadata indicatorCodeMetadata : indicatorMetadata.codes().values()) {
            if (indicatorCodeMetadata.defaultValue()) {
              var indicatorCode = new IndicatorCode();
              indicatorCode.setId(UUID.fromString(indicatorCodeMetadata.id()));
              indicatorCode.setCode(indicatorCodeMetadata.code());
              indicatorCode.setLabel(indicatorCodeMetadata.label());
              indicatorCode.setDeprecated(indicatorCodeMetadata.deprecated());
              indicatorCode.setScope(Scope.valueOf(indicatorCodeMetadata.scope()));
              indicatorCodes.add(indicatorCode);
            }
          }
          indicator.setCodes(indicatorCodes);
        }
      }
      defaultField.setIndicators(indicators);
    }
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
    field.setSubfields(prepareSubfields(fieldElement.get(SUBFIELDS_PROP), fieldMetadata));
    field.setIndicators(prepareIndicators(fieldElement.get(INDICATORS_PROP), fieldMetadata));
    return field;
  }

  private Set<Subfield> prepareSubfields(JsonNode jsonNode, FieldMetadata fieldMetadata) {
    if (jsonNode == null || jsonNode.isEmpty() || fieldMetadata.subfields() == null) {
      return Collections.emptySet();
    }
    Set<Subfield> subfields = new HashSet<>();

    var metadataMap = fieldMetadata.subfields();
    for (JsonNode subfieldElement : jsonNode) {
      var code = getText(subfieldElement, CODE_PROP);
      var subfieldMetadata =
        metadataMap.computeIfAbsent(code, definedCode -> new SubfieldMetadata(code, Scope.STANDARD.name()));
      subfields.add(toSubfield(subfieldElement, subfieldMetadata));
    }

    for (SubfieldMetadata subfieldMetadata : metadataMap.values()) {
      if (subfieldMetadata.defaultValue()) {
        subfields.add(toSubfield(subfieldMetadata));
      }
    }
    return subfields;
  }

  private List<Indicator> prepareIndicators(JsonNode jsonNode, FieldMetadata fieldMetadata) {
    if (jsonNode == null || jsonNode.isEmpty() || !jsonNode.isArray() || fieldMetadata.indicators() == null) {
      return Collections.emptyList();
    }
    List<Indicator> indicators = new ArrayList<>();
    var metadataMap = fieldMetadata.indicators();
    for (JsonNode indicatorElement : jsonNode) {
      var indicatorMetadata =
        metadataMap.computeIfAbsent(String.valueOf(getInt(indicatorElement, ORDER_PROP)), IndicatorMetadata::new);
      var indicator = new Indicator();
      indicator.setId(UUID.fromString(indicatorMetadata.id()));
      indicator.setOrder(indicatorMetadata.order());
      indicator.setLabel(getText(indicatorElement, LABEL_PROP));
      indicator.setCodes(toIndicatorCodes(indicatorElement, indicatorMetadata));
      indicators.add(indicator);
    }

    for (IndicatorMetadata indicatorMetadata : fieldMetadata.indicators().values()) {
      if (indicatorMetadata.defaultValue()) {
        var indicator = new Indicator();
        indicator.setId(UUID.fromString(indicatorMetadata.id()));
        indicator.setOrder(indicatorMetadata.order());
        indicator.setLabel(indicatorMetadata.label());
        List<IndicatorCode> indicatorCodes = new ArrayList<>();
        for (IndicatorCodeMetadata indicatorCodeMetadata : indicatorMetadata.codes().values()) {
          if (indicatorCodeMetadata.defaultValue()) {
            var indicatorCode = new IndicatorCode();
            indicatorCode.setId(UUID.fromString(indicatorCodeMetadata.id()));
            indicatorCode.setCode(indicatorCodeMetadata.code());
            indicatorCode.setLabel(indicatorCodeMetadata.label());
            indicatorCode.setDeprecated(indicatorCodeMetadata.deprecated());
            indicatorCode.setScope(Scope.valueOf(indicatorCodeMetadata.scope()));
            indicatorCodes.add(indicatorCode);
          }
        }
        indicator.setCodes(indicatorCodes);
      }
    }
    return indicators;
  }

  private List<IndicatorCode> toIndicatorCodes(JsonNode indicatorElement, IndicatorMetadata indicatorMetadata) {
    var codesElement = indicatorElement.get(CODES_PROP);
    if (codesElement == null || codesElement.isEmpty() || !codesElement.isArray()) {
      return Collections.emptyList();
    }
    List<IndicatorCode> codes = new ArrayList<>();

    for (JsonNode codeElement : codesElement) {
      var indicatorCodeMetadata = indicatorMetadata.codes().computeIfAbsent(getText(codeElement, CODE_PROP),
        code -> new IndicatorCodeMetadata(code, Scope.STANDARD.name()));
      var indicatorCode = new IndicatorCode();
      indicatorCode.setId(UUID.fromString(indicatorCodeMetadata.id()));
      indicatorCode.setCode(indicatorCodeMetadata.code());
      indicatorCode.setLabel(getText(codeElement, LABEL_PROP));
      indicatorCode.setDeprecated(getBoolean(codeElement, DEPRECATED_PROP));
      indicatorCode.setScope(Scope.valueOf(indicatorCodeMetadata.scope()));
      codes.add(indicatorCode);
    }

    return codes;
  }

  private Subfield toSubfield(SubfieldMetadata subfieldMetadata) {
    var defaultSubfield = new Subfield();
    defaultSubfield.setId(UUID.fromString(subfieldMetadata.id()));
    defaultSubfield.setCode(subfieldMetadata.code());
    defaultSubfield.setLabel(subfieldMetadata.label());
    defaultSubfield.setScope(Scope.valueOf(subfieldMetadata.scope()));
    defaultSubfield.setRepeatable(subfieldMetadata.repeatable());
    defaultSubfield.setDeprecated(subfieldMetadata.deprecated());
    defaultSubfield.setRequired(subfieldMetadata.required());
    return defaultSubfield;
  }

  private Subfield toSubfield(JsonNode subfieldElement, SubfieldMetadata subfieldMetadata) {
    var subfield = new Subfield();
    subfield.setId(UUID.fromString(subfieldMetadata.id()));
    subfield.setScope(Scope.valueOf(subfieldMetadata.scope()));
    subfield.setCode(subfieldMetadata.code());
    subfield.setLabel(getText(subfieldElement, LABEL_PROP));
    subfield.setDeprecated(getBoolean(subfieldElement, DEPRECATED_PROP));
    subfield.setRepeatable(getBoolean(subfieldElement, REPEATABLE_PROP));
    subfield.setRequired(isRequired(subfieldElement, subfieldMetadata));
    return subfield;
  }

  private boolean isRequired(JsonNode fieldElement, SubfieldMetadata subfieldMetadata) {
    return subfieldMetadata.required() != null ? subfieldMetadata.required() : getBoolean(fieldElement, REQUIRED_PROP);
  }

  private boolean isRequired(JsonNode fieldElement, FieldMetadata fieldMetadata) {
    return fieldMetadata.required() != null ? fieldMetadata.required() : getBoolean(fieldElement, REQUIRED_PROP);
  }
}
