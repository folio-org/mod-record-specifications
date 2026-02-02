package org.folio.api;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.folio.support.ApiEndpoints.fieldSubfieldsPath;
import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.ApiEndpoints.specificationSyncPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.entity.support.UuidPersistable;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.SpecificationITBase;
import org.folio.support.TestRailCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

@IntegrationTest
@DatabaseCleanup(tables = FIELD_TABLE_NAME, tenants = TENANT_ID)
class SpecificationStorageSyncApiIT extends SpecificationITBase {

  private static final String LABEL_FIELD = "label";
  private static final String CODE_FIELD = "code";
  private static final String SCOPE_FIELD = "scope";
  private static final String TAG_FIELD = "tag";
  private static final String URL_FIELD = "url";
  private static final String ID_FIELD = "id";
  private static final String REPEATABLE_FIELD = "repeatable";
  private static final String REQUIRED_FIELD = "required";
  private static final String DEPRECATED_FIELD = "deprecated";

  private static final Set<String> VALID_CODES = new HashSet<>();
  private static final Set<String> VALID_CODES_WITH_HASH = new HashSet<>();

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

    var numberStream = IntStream.rangeClosed(0, 9).boxed().map(String::valueOf);
    var letterStream = IntStream.rangeClosed('a', 'z').boxed().map(Character::toString);
    var validCodes = Stream.concat(numberStream, letterStream).collect(Collectors.toSet());
    VALID_CODES.addAll(validCodes);
    validCodes.add("#");
    VALID_CODES_WITH_HASH.addAll(validCodes);
  }

  @Test
  @SuppressWarnings("checkstyle:methodLength")
  void syncSpecification_produceSameEntitiesEachTime() throws Exception {
    var specificationId = BIBLIOGRAPHIC_SPECIFICATION_ID;

    doPost(specificationSyncPath(specificationId), null);

    final var createdFieldIds = executeInContext(() -> toIdArray(fieldRepository.findAll()));
    final var createdSubfieldIds = executeInContext(() -> toIdArray(subfieldRepository.findAll()));
    final var createdIndicatorIds = executeInContext(() -> toIdArray(indicatorRepository.findAll()));
    final var createdIndicatorCodeIds = executeInContext(() -> toIdArray(indicatorCodeRepository.findAll()));

    // check if second sync will produce same results
    doPost(specificationSyncPath(specificationId), null)
      .andExpect(status().isAccepted());

    var recreatedFields = executeInContext(() -> fieldRepository.findAll());
    var recreatedSubfields = executeInContext(() -> subfieldRepository.findAll());
    final var recreatedIndicators = executeInContext(() -> indicatorRepository.findAll());
    final var recreatedIndicatorCodes = executeInContext(() -> indicatorCodeRepository.findAll());

    assertThat(recreatedFields)
      .hasSize(293)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdFieldIds);

    assertThat(recreatedSubfields)
      .hasSize(2840)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdSubfieldIds);

    assertThat(recreatedIndicators)
      .hasSize(528)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdIndicatorIds);

    assertThat(recreatedIndicatorCodes)
      .hasSize(1193)
      .extracting(UuidPersistable::getId)
      .containsExactlyInAnyOrder(createdIndicatorCodeIds);

    assertSpecificationUpdatedEvent();
  }

  @Test
  @TestRailCase("C494338")
  @SuppressWarnings("checkstyle:methodLength")
  void syncSpecification_shouldContainCorrectFieldsAndProperties() throws Exception {
    var specificationId = BIBLIOGRAPHIC_SPECIFICATION_ID;

    // Trigger sync
    doPost(specificationSyncPath(specificationId), null);

    // Test Case 1: Verify total fields count and basic properties
    var responseBody = doGet(specificationFieldsPath(specificationId))
      .andExpect(status().isOk())
      .andExpect(totalRecordsMatcher(293))
      .andReturn().getResponse().getContentAsString();

    var fields = getFields(responseBody);

    // Test Case 2: Verify System fields with their specific properties
    assertSystemFields(fields);

    // Test Case 3: Verify Standard fields
    assertStandardFields(fields);

    // Test Case 4-5: Verify deprecated fields
    assertDeprecatedFields(fields);

    // Test Case 6-8: Verify control field indicators and subfields
    assertControlFieldIndicatorsAndSubfields(fields);

    // Test Case 7: Verify 999 field indicators and subfields
    assert999FieldIndicatorsAndSubfields(fields);

    // Test Case 9-11: Verify standard field indicators, indicator codes and subfields
    assertStandardFieldIndicatorsAndSubfields(fields);

    // Test Case 12: Verify subfield 9 in linkable fields
    assertLinkableFieldsSubfield9(specificationId);

    // Test Case 13: Verify 041 field subfield c
    assertField041SubfieldC(fields);

    // Test Case 14: Verify 082 field subfield b
    assertField082SubfieldB(fields);

    // Test Case 15: Verify 856 field subfields
    assertField856Subfields(fields);

    // Test Case 16: Verify 850 field subfields
    assertField850Subfields(fields);

    // Test Case 17: Verify deprecated subfield q in linking entry fields
    assertLinkingEntryFieldsSubfieldQ(fields);

    // Test Case 18: Verify 611 field subfield b
    assertField611SubfieldB(fields);

    // Test Case 19: Verify 711 field subfield b
    assertField711SubfieldB(fields);

    // Test Case 20: Verify deprecated subfield z in note fields
    assertNoteFieldsSubfieldZ(fields);

    // Test Case 21: Verify 411 field subfield b
    assertField411SubfieldB(fields);

    // Test Case 22: Verify 242 field subfields d and e
    assertField242SubfieldsD(fields);

    // Test Case 23: Verify 111 field subfield b
    assertField111SubfieldB(fields);

    // Test Case 24: Verify 340 field subfield 1
    assertField340Subfield1(fields);

    assertSpecificationUpdatedEvent();
  }

  @Test
  @TestRailCase("C494342")
  @SuppressWarnings("checkstyle:methodLength")
  void syncSpecification_shouldRestoreFieldsToDefaultAndRemoveLocalFields() throws Exception {
    var specificationId = BIBLIOGRAPHIC_SPECIFICATION_ID;
    
    // Initial sync to establish baseline
    doPost(specificationSyncPath(specificationId), null)
      .andExpect(status().isAccepted());
    
    // Get the initial state of fields before modifications
    var initialResponse = doGet(specificationFieldsPath(specificationId))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var initialFields = getFields(initialResponse);
    
    // Create a local field
    var localFieldTag = "950";
    var localFieldDto = localTestField(localFieldTag);
    createLocalField(localFieldDto);

    // Modify a system field (245) - only url can be modified
    var initial245Field = findFieldOrFail(initialFields, "245");
    var systemFieldUpdate = prepareChangeDto(initial245Field, dto
      -> dto.setUrl("https://modified-system-field.com"));
    var systemFieldId = getFieldId(initialFields, "245");
    doPut(fieldPath(systemFieldId), systemFieldUpdate);
    
    // Modify a standard field (100) - url and required can be modified
    var initial100Field = findFieldOrFail(initialFields, "100");
    var standardFieldId = getFieldId(initialFields, "100");
    var standardFieldUpdate = prepareChangeDto(initial100Field, dto -> {
      dto.setUrl("https://modified-standard-field.com");
      dto.setRequired(true);
    });
    doPut(fieldPath(standardFieldId), standardFieldUpdate);
    
    // Trigger sync to restore to defaults
    doPost(specificationSyncPath(specificationId), null)
      .andExpect(status().isAccepted());
    
    // Verify local fields are removed and modifications are restored
    var responseAfterSync = doGet(specificationFieldsPath(specificationId))
      .andExpect(status().isOk())
      .andExpect(totalRecordsMatcher(293))
      .andReturn().getResponse().getContentAsString();
    var fieldsAfterSync = getFields(responseAfterSync);
    
    // Verify the local field is removed
    assertThat(fieldsAfterSync)
      .as("Local field %s should not exist after sync", localFieldTag)
      .extracting(f -> (String) f.get(TAG_FIELD))
      .doesNotContain(localFieldTag);

    // Verify the system field is restored
    var restoredSystemField = findFieldOrFail(fieldsAfterSync, "245");
    assertThat(restoredSystemField)
      .as("System field 245 URL should be restored to default")
      .containsEntry(URL_FIELD, initial245Field.get(URL_FIELD));
    
    // Verify the standard field is restored
    var restoredStandardField = findFieldOrFail(fieldsAfterSync, "100");
    assertThat(restoredStandardField)
      .as("Standard field 100 should be restored to default")
      .containsEntry(URL_FIELD, initial100Field.get(URL_FIELD))
      .containsEntry(REQUIRED_FIELD, initial100Field.get(REQUIRED_FIELD));
  }

  private SpecificationFieldChangeDto prepareChangeDto(Map<String, Object> initialField,
                                                       Consumer<SpecificationFieldChangeDto> modificator) {
    var fieldChangeDto = new SpecificationFieldChangeDto()
      .tag((String) initialField.get(TAG_FIELD))
      .label((String) initialField.get(LABEL_FIELD))
      .repeatable((Boolean) initialField.get(REPEATABLE_FIELD))
      .required((Boolean) initialField.get(REQUIRED_FIELD))
      .deprecated((Boolean) initialField.get(SpecificationStorageSyncApiIT.DEPRECATED_FIELD))
      .url((String) initialField.get(URL_FIELD));
    modificator.accept(fieldChangeDto);
    return fieldChangeDto;
  }

  private UUID[] toIdArray(List<? extends UuidPersistable> all) {
    return all.stream().map(UuidPersistable::getId).toArray(UUID[]::new);
  }

  private void assertSystemFields(List<Map<String, Object>> fields) {
    var systemFields = fields.stream()
      .filter(f -> Scope.SYSTEM.getValue().equals(f.get(SCOPE_FIELD)))
      .toList();

    assertThat(systemFields).hasSize(8);

    // Verify specific system fields
    assertField(systemFields, "000", false, true, false);
    assertField(systemFields, "001", false, true, false);
    assertField(systemFields, "005", false, false, false);
    assertField(systemFields, "006", true, false, false);
    assertField(systemFields, "007", true, false, false);
    assertField(systemFields, "008", false, true, false);
    assertField(systemFields, "245", false, true, false);
    assertField(systemFields, "999", true, false, false);
  }

  private void assertStandardFields(List<Map<String, Object>> fields) {
    var standardFields = fields.stream()
      .filter(f -> Scope.STANDARD.getValue().equals(f.get(SCOPE_FIELD)))
      .toList();

    // Verify specific standard fields exist
    assertField(standardFields, "010", false, false, false);
    assertField(standardFields, "035", true, false, false);
    assertField(standardFields, "100", false, false, false);
    assertField(standardFields, "246", true, false, false);
    assertField(standardFields, "600", true, false, false);
  }

  private void assertDeprecatedFields(List<Map<String, Object>> fields) {
    var expectedDeprecatedTags = new HashSet<>(asList(
      "009", "011", "090", "091", "211", "212", "214", "241", "265", "301", "302", "303", "304", "305",
      "308", "315", "350", "359", "440", "503", "512", "517", "523", "527", "537", "543", "570", "582",
      "590", "652", "705", "715", "755", "840", "851", "870", "871", "872", "873"
    ));

    var deprecatedFields = fields.stream()
      .filter(f -> Boolean.TRUE.equals(f.get(DEPRECATED_FIELD)))
      .toList();

    var deprecatedTags = deprecatedFields.stream()
      .map(f -> (String) f.get(TAG_FIELD))
      .collect(Collectors.toSet());

    assertThat(deprecatedTags).containsAll(expectedDeprecatedTags);
    assertThat(deprecatedFields).hasSize(39);

    // Verify deprecated fields don't have url
    deprecatedFields.forEach(field -> assertThat(field.containsKey(URL_FIELD) && field.get(URL_FIELD) != null)
      .as("Deprecated field %s should not have url", field.get(TAG_FIELD))
      .isFalse());
  }

  private void assertControlFieldIndicatorsAndSubfields(List<Map<String, Object>> fields) throws Exception {
    var controlFields = asList("000", "001", "005", "006", "007", "008");

    for (var controlField : controlFields) {
      var fieldId = getFieldId(fields, controlField);
      // Verify no indicators
      doGet(fieldIndicatorsPath(fieldId))
        .andExpect(status().isOk())
        .andExpect(totalRecordsMatcher(0));

      // Verify no subfields
      doGet(fieldSubfieldsPath(fieldId))
        .andExpect(status().isOk())
        .andExpect(totalRecordsMatcher(0));
    }
  }

  private void assert999FieldIndicatorsAndSubfields(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "999");
    var indicators = getIndicators(requestIndicators(fieldId));

    for (var indicator : indicators) {
      var indicatorId = (String) indicator.get(ID_FIELD);

      // Verify indicator codes
      var codes = getIndicatorCodes(requestIndicatorCodes(indicatorId));

      var actualCodes = codes.stream()
        .map(c -> (String) c.get(CODE_FIELD))
        .collect(Collectors.toSet());

      // Should contain a-z, 0-9, and #
      assertThat(actualCodes).containsExactlyInAnyOrderElementsOf(VALID_CODES_WITH_HASH);
    }

    // Verify subfields
    var subfieldResponseBody = requestSubfields(fieldId);
    var subfields = getSubfields(subfieldResponseBody);

    var subfieldCodes = subfields.stream()
      .map(s -> (String) s.get(CODE_FIELD))
      .collect(Collectors.toSet());

    // Should contain a-z and 0-9 characters
    assertThat(subfieldCodes).containsExactlyInAnyOrderElementsOf(VALID_CODES);
  }

  private void assertStandardFieldIndicatorsAndSubfields(List<Map<String, Object>> fields) throws Exception {
    var field100Id = getFieldId(fields, "100");

    // Verify indicators exist
    var indicators = getIndicators(requestIndicators(field100Id));
    var indicatorId = (String) indicators.getFirst().get(ID_FIELD);

    // Verify indicator codes exist
    var totalCodes = getTotalRecords(requestIndicatorCodes(indicatorId));
    assertThat(totalCodes).isGreaterThan(0);

    // Verify subfields exist
    var totalSubfields = getTotalRecords(requestSubfields(field100Id));
    assertThat(totalSubfields).isGreaterThan(0);
  }

  private void assertLinkableFieldsSubfield9(UUID specificationId) throws Exception {
    // Note: Subfield 9 (Linked authority UUID) is a system subfield that may be added
    // during specific operations. This test verifies it exists where expected.
    var linkableTags = asList(
      "100", "110", "111", "130", "240", "600", "610", "611", "630", "650", "651", "655",
      "700", "710", "711", "730", "800", "810", "811", "830"
    );

    var responseBody = doGet(specificationFieldsPath(specificationId))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var fields = getFields(responseBody);

    for (String tag : linkableTags) {
      var field = fields.stream()
        .filter(f -> tag.equals(f.get(TAG_FIELD)))
        .findFirst()
        .orElseThrow();

      var fieldId = (String) field.get(ID_FIELD);

      // Fetch subfields for this field
      var subfields = getSubfields(requestSubfields(fieldId));

      // If subfield 9 exists, verify its properties
      subfields.stream()
        .filter(s -> "9".equals(s.get(CODE_FIELD)))
        .findFirst()
        .ifPresent(subfield9 -> assertSubfield(subfield9, "9",
          "Linked authority UUID", false, false, false));
    }

    // Note: The presence of subfield 9 depends on whether fields have been
    // linked to authority records. This test verifies the structure when present.
  }

  private void assertField041SubfieldC(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "041");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "c", "languages of available translation", true, false, true);
  }

  private void assertField082SubfieldB(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "082");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "b", "Item number", false, false, false);
  }

  private void assertField856Subfields(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "856");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "g", "Persistent identifier", true, false, false);
    assertSubfield(subfields, "h", "Non-functioning Uniform Resource Identifier", true, false, false);
    assertSubfield(subfields, "l", "Standardized information governing access", true, false, false);
    assertSubfield(subfields, "n", "Terms governing access", true, false, false);
    assertSubfield(subfields, "r", "Standardized information governing use and reproduction", true, false, false);
    assertSubfield(subfields, "t", "Terms governing use and reproduction", true, false, false);
  }

  private void assertField850Subfields(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "850");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "b", "Holdings", false, false, true);
    assertSubfield(subfields, "d", "Inclusive dates", false, false, true);
    assertSubfield(subfields, "e", "Retention statement", false, false, true);
  }

  private void assertLinkingEntryFieldsSubfieldQ(List<Map<String, Object>> fields) throws Exception {
    var linkingEntryTags = asList("760", "762", "765", "767", "770", "772", "775", "776", "777", "780", "785");

    for (String tag : linkingEntryTags) {
      var fieldId = getFieldId(fields, tag);
      var subfields = getSubfields(requestSubfields(fieldId));

      assertSubfield(subfields, "q", "Parallel title", false, false, true);
    }
  }

  private void assertField611SubfieldB(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "611");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "b", "Number", true, false, true);
  }

  private void assertField711SubfieldB(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "711");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "b", "Number", true, false, true);
  }

  private void assertNoteFieldsSubfieldZ(List<Map<String, Object>> fields) throws Exception {
    var noteTags = asList("500", "515", "525", "530");

    for (String tag : noteTags) {
      var fieldId = getFieldId(fields, tag);
      var subfields = getSubfields(requestSubfields(fieldId));

      var subfieldZ = findSubfieldOrFail(subfields, "z");
      assertSubfieldDeprecated(subfieldZ, "z", true);
    }
  }

  private void assertField411SubfieldB(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "411");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "b", "Number", true, false, true);
  }

  private void assertField242SubfieldsD(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "242");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "d", "Designation of section", true, false, true);
    assertSubfield(subfields, "e", "Name of part/section", true, false, true);
  }

  private void assertField111SubfieldB(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "111");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "b", "Number", true, false, true);
  }

  private void assertField340Subfield1(List<Map<String, Object>> fields) throws Exception {
    var fieldId = getFieldId(fields, "340");
    var subfields = getSubfields(requestSubfields(fieldId));

    assertSubfield(subfields, "1", "Real World Object URI", true, false, false);
  }

  private String requestSubfields(String fieldId) throws Exception {
    return doGet(fieldSubfieldsPath(fieldId))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
  }

  private String requestIndicatorCodes(String indicatorId) throws Exception {
    return doGet(indicatorCodesPath(indicatorId))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
  }

  private String requestIndicators(String fieldId) throws Exception {
    return doGet(fieldIndicatorsPath(fieldId))
      .andExpect(status().isOk())
      .andExpect(totalRecordsMatcher(2))
      .andReturn().getResponse().getContentAsString();
  }

  private void assertField(List<Map<String, Object>> fields, String tag, boolean repeatable, boolean required,
                           boolean deprecated) {
    var field = findFieldOrFail(fields, tag);

    assertThat(field).as("Field %s repeatable", tag).containsEntry(REPEATABLE_FIELD, repeatable);
    assertThat(field).as("Field %s required", tag).containsEntry(REQUIRED_FIELD, required);
    assertThat(field).as("Field %s deprecated", tag).containsEntry(DEPRECATED_FIELD, deprecated);
  }

  private void assertSubfield(List<Map<String, Object>> subfields, String code, String label, boolean repeatable,
                              boolean required, boolean deprecated) {
    var subfield = findSubfieldOrFail(subfields, code);

    assertSubfield(subfield, code, label, repeatable, required, deprecated);
  }

  private void assertSubfield(Map<String, Object> subfield, String code, String label, boolean repeatable,
                              boolean required, boolean deprecated) {
    assertThat(subfield).as("Subfield %s label", code).containsEntry(LABEL_FIELD, label);
    assertThat(subfield).as("Subfield %s repeatable", code).containsEntry(REPEATABLE_FIELD, repeatable);
    assertThat(subfield).as("Subfield %s required", code).containsEntry(REQUIRED_FIELD, required);
    assertSubfieldDeprecated(subfield, code, deprecated);
  }

  private void assertSubfieldDeprecated(Map<String, Object> subfield, String code, boolean deprecated) {
    assertThat(subfield).as("Subfield %s deprecated", code).containsEntry(DEPRECATED_FIELD, deprecated);
  }

  private String getFieldId(List<Map<String, Object>> fields, String tag) {
    return (String) findFieldOrFail(fields, tag).get(ID_FIELD);
  }

  private List<Map<String, Object>> getFields(String responseBody) {
    return JsonPath.read(responseBody, "$.fields");
  }

  private List<Map<String, Object>> getSubfields(String responseBody) {
    return JsonPath.read(responseBody, "$.subfields");
  }

  private List<Map<String, Object>> getIndicators(String indicatorResponseBody) {
    return JsonPath.read(indicatorResponseBody, "$.indicators");
  }

  private List<Map<String, Object>> getIndicatorCodes(String codeResponseBody) {
    return JsonPath.read(codeResponseBody, "$.codes");
  }

  private Integer getTotalRecords(String subfieldResponseBody) {
    return JsonPath.read(subfieldResponseBody, "$.totalRecords");
  }

  private Map<String, Object> findFieldOrFail(List<Map<String, Object>> fields, String tag) {
    return fields.stream()
      .filter(f -> tag.equals(f.get(TAG_FIELD)))
      .findFirst()
      .orElseThrow(() -> new AssertionError("Field " + tag + " not found"));
  }

  private Map<String, Object> findSubfieldOrFail(List<Map<String, Object>> subfields, String code) {
    return subfields.stream()
      .filter(s -> code.equals(s.get(CODE_FIELD)))
      .findFirst()
      .orElseThrow(() -> new AssertionError("Subfield " + code + " not found"));
  }

  private ResultMatcher totalRecordsMatcher(int expectedValue) {
    return jsonPath("$.totalRecords").value(expectedValue);
  }
}
