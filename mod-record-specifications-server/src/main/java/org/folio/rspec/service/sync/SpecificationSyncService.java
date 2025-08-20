package org.folio.rspec.service.sync;

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
import static org.folio.rspec.utils.JsonUtils.getBoolean;
import static org.folio.rspec.utils.JsonUtils.getInt;
import static org.folio.rspec.utils.JsonUtils.getText;

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
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
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
    var fields = evaluatorFetcher(specification, specificationMetadata);
    metadataService.saveSpecificationMetadata(specificationMetadata);
    specificationFieldService.syncFields(specification, fields);
  }

  private List<Field> evaluatorFetcher(Specification specification, SpecificationMetadata specificationMetadata) {
    var fieldsMetadata = specificationMetadata.getFields();
    var specificationFields = specificationFetcher.fetch(specification.getFamily(), specification.getProfile());
    if (specificationFields != null && !specificationFields.isEmpty()) {
      return syncFields(specification, specificationFields, fieldsMetadata, specificationMetadata);
    }
    return new ArrayList<>();
  }

  private List<Field> syncFields(Specification specification, ArrayNode specificationFields,
                                 Map<String, FieldMetadata> fieldsMetadata,
                                 SpecificationMetadata specificationMetadata) {
    var fieldMap = new HashMap<String, Field>();

    for (var fieldElement : specificationFields) {
      var field = populateField(fieldElement, fieldsMetadata, specificationMetadata, specification);
      fieldMap.merge(field.getTag(), field, preferNonDeprecated(Field::isDeprecated));
    }

    for (var value : fieldsMetadata.values()) {
      if (Boolean.TRUE.equals(value.defaultValue())) {
        var field = toField(value);
        field.setSpecification(specification);
        fieldMap.merge(field.getTag(), field, preferNonDeprecated(Field::isDeprecated));
      }
    }

    return new ArrayList<>(fieldMap.values());
  }

  private Field populateField(JsonNode fieldElement, Map<String, FieldMetadata> fieldsMetadata,
                              SpecificationMetadata specificationMetadata, Specification specification) {
    var tag = getText(fieldElement, TAG_PROP);
    var fieldMetadata =
      fieldsMetadata.computeIfAbsent(tag, definedTag -> new FieldMetadata(definedTag, Scope.STANDARD.name()));
    var baseFieldUrlFormat = specificationMetadata.getUrlFormat();

    if (fieldElement == null) {
      return toField(fieldMetadata);  // original method
    } else {
      var field = new Field();
      field.setId(UUID.fromString(fieldMetadata.id()));
      field.setTag(fieldMetadata.tag());
      field.setScope(Scope.valueOf(fieldMetadata.scope()));
      field.setLabel(getText(fieldElement, LABEL_PROP));
      var isDeprecated = getBoolean(fieldElement, DEPRECATED_PROP);
      field.setDeprecated(isDeprecated);
      if (!isDeprecated) {
        field.setUrl(baseFieldUrlFormat.formatted(field.getTag()));
      }
      field.setRepeatable(getBoolean(fieldElement, REPEATABLE_PROP));
      field.setRequired(isRequired(fieldElement, fieldMetadata));
      field.setSubfields(prepareSubfields(fieldElement.get(SUBFIELDS_PROP), fieldMetadata));
      field.setIndicators(prepareIndicators(fieldElement.get(INDICATORS_PROP), fieldMetadata));
      field.setSpecification(specification);
      return field;
    }
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

    var subfields = new HashSet<Subfield>();
    if (fieldMetadata.subfields() != null) {
      for (var subfieldMetadata : fieldMetadata.subfields().values()) {
        if (Boolean.TRUE.equals(subfieldMetadata.defaultValue())) {
          subfields.add(toSubfield(subfieldMetadata));
        }
      }
      defaultField.setSubfields(subfields);
    }

    if (fieldMetadata.indicators() != null) {
      var indicators = new ArrayList<Indicator>();
      populateDefaultIndicators(fieldMetadata, indicators);
      defaultField.setIndicators(indicators);
    }
    return defaultField;
  }

  private Set<Subfield> prepareSubfields(JsonNode jsonNode, FieldMetadata fieldMetadata) {
    if (jsonNode == null || jsonNode.isEmpty()) {
      return Collections.emptySet();
    }

    var subfieldMap = new HashMap<String, Subfield>();
    var subfieldsMetadata = fieldMetadata.subfields() == null
                            ? new HashMap<String, SubfieldMetadata>()
                            : fieldMetadata.subfields();
    for (var subfieldElement : jsonNode) {
      var code = getText(subfieldElement, CODE_PROP);
      var subfieldMetadata = subfieldsMetadata.computeIfAbsent(code,
        definedCode -> new SubfieldMetadata(code, Scope.STANDARD.name()));
      var subfield = toSubfield(subfieldElement, subfieldMetadata);

      subfieldMap.merge(subfield.getCode(), subfield, preferNonDeprecated(Subfield::isDeprecated));
    }

    for (var subfieldMetadata : subfieldsMetadata.values()) {
      if (Boolean.TRUE.equals(subfieldMetadata.defaultValue())) {
        var subfield = toSubfield(subfieldMetadata);
        subfieldMap.merge(subfield.getCode(), subfield, preferNonDeprecated(Subfield::isDeprecated));
      }
    }

    return new HashSet<>(subfieldMap.values());
  }

  private List<Indicator> prepareIndicators(JsonNode jsonNode, FieldMetadata fieldMetadata) {
    if (jsonNode == null || jsonNode.isEmpty() || !jsonNode.isArray()) {
      return Collections.emptyList();
    }
    var indicators = new ArrayList<Indicator>();
    var indicatorsMetadata = fieldMetadata.indicators() == null
                             ? new HashMap<String, IndicatorMetadata>()
                             : fieldMetadata.indicators();
    for (var indicatorElement : jsonNode) {
      var order = String.valueOf(getInt(indicatorElement, ORDER_PROP));
      var indicatorMetadata = indicatorsMetadata.computeIfAbsent(order, IndicatorMetadata::new);
      var indicator = new Indicator();
      indicator.setId(UUID.fromString(indicatorMetadata.id()));
      indicator.setOrder(indicatorMetadata.order());
      indicator.setLabel(getText(indicatorElement, LABEL_PROP));
      indicator.setCodes(toIndicatorCodes(indicatorElement, indicatorMetadata));
      indicators.add(indicator);
    }

    populateDefaultIndicators(fieldMetadata, indicators);
    return indicators;
  }

  private void populateDefaultIndicators(FieldMetadata fieldMetadata, List<Indicator> indicators) {
    for (var indicatorMetadata : fieldMetadata.indicators().values()) {
      if (Boolean.TRUE.equals(indicatorMetadata.defaultValue())) {
        var indicator = new Indicator();
        indicator.setId(UUID.fromString(indicatorMetadata.id()));
        indicator.setOrder(indicatorMetadata.order());
        indicator.setLabel(indicatorMetadata.label());
        var indicatorCodes = new ArrayList<IndicatorCode>();
        for (var indicatorCodeMetadata : indicatorMetadata.codes().values()) {
          if (Boolean.TRUE.equals(indicatorCodeMetadata.defaultValue())) {
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
        indicators.add(indicator);
      }
    }
  }

  private List<IndicatorCode> toIndicatorCodes(JsonNode indicatorElement, IndicatorMetadata indicatorMetadata) {
    var codesElement = indicatorElement.get(CODES_PROP);
    if (codesElement == null || codesElement.isEmpty() || !codesElement.isArray()) {
      return Collections.emptyList();
    }

    var indicatorCodeMap = new HashMap<String, IndicatorCode>();
    for (var codeElement : codesElement) {
      var indicatorCodeMetadata = indicatorMetadata.codes().computeIfAbsent(getText(codeElement, CODE_PROP),
        code -> new IndicatorCodeMetadata(code, Scope.STANDARD.name()));
      var indicatorCode = new IndicatorCode();
      indicatorCode.setId(UUID.fromString(indicatorCodeMetadata.id()));
      indicatorCode.setCode(indicatorCodeMetadata.code());
      indicatorCode.setLabel(getText(codeElement, LABEL_PROP));
      indicatorCode.setDeprecated(getBoolean(codeElement, DEPRECATED_PROP));
      indicatorCode.setScope(Scope.valueOf(indicatorCodeMetadata.scope()));

      indicatorCodeMap.merge(indicatorCode.getCode(), indicatorCode, preferNonDeprecated(IndicatorCode::isDeprecated));
    }

    return new ArrayList<>(indicatorCodeMap.values());
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

  private static <T> BinaryOperator<T> preferNonDeprecated(Predicate<T> isDeprecatedPredicate) {
    return (existing, incoming) -> existing != null && isDeprecatedPredicate.test(existing) ? incoming : existing;
  }
}
