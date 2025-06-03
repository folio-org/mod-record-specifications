package org.folio.api;

import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.rspec.domain.entity.Indicator.INDICATOR_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.folio.support.ApiEndpoints.fieldSubfieldsPath;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.folio.support.builders.FieldBuilder.local;
import static org.folio.support.builders.FieldBuilder.standard;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ResourceValidationFailedException;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.SpecificationITBase;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
@DatabaseCleanup(tables = {INDICATOR_TABLE_NAME, FIELD_TABLE_NAME}, tenants = TENANT_ID)
class SpecificationStorageFieldsApiIT extends SpecificationITBase {

  @Autowired
  private FieldRepository fieldRepository;

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void deleteField_shouldReturn204AndDeleteLocalField() throws Exception {
    var createdFieldId = createLocalField(local().buildChangeDto());

    doDelete(fieldPath(createdFieldId));

    doGet(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("$.fields.[*].id", not(hasItem(createdFieldId))));

    assertSpecificationUpdatedEvents(2);
  }

  @Test
  void deleteField_shouldReturn404WhenFieldNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryDelete(fieldPath(notExistId))
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("field definition with ID [%s] was not found.".formatted(notExistId))));
  }

  @Test
  void updateField_shouldReturn201AndUpdateLocalField() throws Exception {
    var localTestField = local().buildChangeDto();
    var createdFieldId = createLocalField(localTestField);

    localTestField.setDeprecated(true);
    localTestField.setUrl("http://www.viverra.com");

    doPut(fieldPath(createdFieldId), localTestField);

    doGet(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("$.fields.[*]", hasItem(Matchers.<Map<String, Object>>allOf(
        hasEntry("id", createdFieldId),
        hasEntry("deprecated", true),
        hasEntry("url", "http://www.viverra.com")
      ))));

    assertSpecificationUpdatedEvents(2);
  }

  @Test
  void updateField_shouldReturn400WhenFieldIsNotAllowedToUpdate() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(standard().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();

    tryPut(fieldPath(createdFieldId), standard().label("changed").buildChangeDto())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'label' modification is not allowed for standard scope.")));
  }

  @Test
  void deleteField_shouldReturn400WhenFieldIsNotAllowedToDelete() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(standard().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();

    tryDelete(fieldPath(createdFieldId))
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("A standard scope field cannot be deleted.")));
  }

  @Test
  void updateField_shouldReturn400WhenFieldForTagAlreadyExist() throws Exception {
    var field1Tag = "105";
    var field2Tag = "205";
    var field2Id = executeInContext(
      () -> {
        var entity1 = standard().id(UUID.randomUUID())
          .tag(field1Tag)
          .specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity();
        var entity2 = local().id(UUID.randomUUID())
          .tag(field2Tag)
          .specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity();
        fieldRepository.saveAll(List.of(entity1, entity2));
        return entity2.getId();
      });

    tryPut(fieldPath(field2Id), local().tag(field1Tag).buildChangeDto())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'tag' must be unique.")));
  }

  @Test
  void updateField_shouldReturn404WhenFieldNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPut(fieldPath(notExistId), local().buildChangeDto())
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("field definition with ID [%s] was not found.".formatted(notExistId))));
  }

  @Test
  void getFieldIndicators_shouldReturn200AndAllIndicatorsForField() throws Exception {
    var fieldId = createLocalField("103");
    var ind1 = localTestIndicator(1);
    var ind2 = localTestIndicator(2);
    doPost(fieldIndicatorsPath(fieldId), ind1);
    doPost(fieldIndicatorsPath(fieldId), ind2);

    doGet(fieldIndicatorsPath(fieldId))
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("indicators.size()", is(2)))
      .andExpect(jsonPath("indicators.[*].id", everyItem(notNullValue())))
      .andExpect(jsonPath("indicators.[*].fieldId", everyItem(is(fieldId))))
      .andExpect(jsonPath("indicators.[*].order", hasItems(ind1.getOrder(), ind2.getOrder())))
      .andExpect(jsonPath("indicators.[*].label", hasItems(ind1.getLabel(), ind2.getLabel())))
      .andExpect(jsonPath("indicators.[*].metadata.createdDate", everyItem(notNullValue())))
      .andExpect(jsonPath("indicators.[*].metadata.createdByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("indicators.[*].metadata.updatedByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("indicators.[*].metadata.updatedDate", everyItem(notNullValue())));
  }

  @Test
  void createFieldLocalIndicator_shouldReturn201AndCreatedIndicator() throws Exception {
    var fieldId = createLocalField("104");
    var dto = localTestIndicator(1);

    var result = doPost(fieldIndicatorsPath(fieldId), dto)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()))
      .andExpect(jsonPath("$.fieldId", is(fieldId)))
      .andExpect(jsonPath("$.order", is(dto.getOrder())))
      .andExpect(jsonPath("$.label", is(dto.getLabel())))
      .andExpect(jsonPath("$.metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.createdByUserId", is(USER_ID)))
      .andExpect(jsonPath("$.metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.updatedByUserId", is(USER_ID)))
      .andReturn();

    var createdIndicatorId = getRecordId(result);

    doGet(fieldIndicatorsPath(fieldId))
      .andExpect(jsonPath("$.indicators.[*].id", hasItem(createdIndicatorId)));

    assertSpecificationUpdatedEvents(2);
  }

  @Test
  void createFieldLocalIndicator_shouldReturn400WhenFieldIndicatorOrderAlreadyExist() throws Exception {
    var fieldId = createLocalField("105");
    var dto = localTestIndicator(1);

    doPost(fieldIndicatorsPath(fieldId), dto);

    tryPost(fieldIndicatorsPath(fieldId), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(DataIntegrityViolationException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.DUPLICATE_FIELD_INDICATOR.getType())))
      .andExpect(errorMessageMatch(is("The 'order' must be unique.")));
  }

  @Test
  void createFieldLocalIndicator_shouldReturn400WhenControlField() throws Exception {
    var fieldId = createLocalField("005");
    var dto = localTestIndicator(1);

    tryPost(fieldIndicatorsPath(fieldId), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(ResourceValidationFailedException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.CONTROL_FIELD_RESOURCE_NOT_ALLOWED.getType())))
      .andExpect(errorMessageMatch(is("Cannot define indicators for 00X control fields.")));
  }

  @Test
  void getFieldSubfields_shouldReturn200AndAllSubfieldsForField() throws Exception {
    var fieldId = createLocalField("103");
    var sub1 = localTestSubfield("a", "Subfield a");
    var sub2 = localTestSubfield("1", "Subfield 1");
    doPost(fieldSubfieldsPath(fieldId), sub1);
    doPost(fieldSubfieldsPath(fieldId), sub2);

    doGet(fieldSubfieldsPath(fieldId))
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("subfields.size()", is(2)))
      .andExpect(jsonPath("subfields.[*].id", everyItem(notNullValue())))
      .andExpect(jsonPath("subfields.[*].fieldId", everyItem(is(fieldId))))
      .andExpect(jsonPath("subfields.[*].code", hasItems(sub1.getCode(), sub2.getCode())))
      .andExpect(jsonPath("subfields.[*].label", hasItems(sub1.getLabel(), sub2.getLabel())))
      .andExpect(jsonPath("subfields.[*].required", everyItem(notNullValue())))
      .andExpect(jsonPath("subfields.[*].repeatable", everyItem(notNullValue())))
      .andExpect(jsonPath("subfields.[*].deprecated", everyItem(notNullValue())))
      .andExpect(jsonPath("subfields.[*].metadata.createdDate", everyItem(notNullValue())))
      .andExpect(jsonPath("subfields.[*].metadata.createdByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("subfields.[*].metadata.updatedByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("subfields.[*].metadata.updatedDate", everyItem(notNullValue())));
  }

  @Test
  void createFieldLocalSubfield_shouldReturn201AndCreatedSubfield() throws Exception {
    var fieldId = createLocalField("104");
    var dto = localTestSubfield("a", "Subfield a");

    var result = doPost(fieldSubfieldsPath(fieldId), dto)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()))
      .andExpect(jsonPath("$.fieldId", is(fieldId)))
      .andExpect(jsonPath("$.code", is(dto.getCode())))
      .andExpect(jsonPath("$.label", is(dto.getLabel())))
      .andExpect(jsonPath("$.required", is(dto.getRequired())))
      .andExpect(jsonPath("$.repeatable", is(dto.getRepeatable())))
      .andExpect(jsonPath("$.deprecated", is(dto.getDeprecated())))
      .andExpect(jsonPath("$.metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.createdByUserId", is(USER_ID)))
      .andExpect(jsonPath("$.metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.updatedByUserId", is(USER_ID)))
      .andReturn();

    var createdSubfieldId = getRecordId(result);

    doGet(fieldSubfieldsPath(fieldId))
      .andExpect(jsonPath("$.subfields.[*].id", hasItem(createdSubfieldId)));

    assertSpecificationUpdatedEvents(2);
  }

  @Test
  void createFieldLocalSubfield_shouldReturn400_whenDuplicateSubfieldCode() throws Exception {
    var fieldId = createLocalField("104");
    var dto = localTestSubfield("a", "Subfield a");

    doPost(fieldSubfieldsPath(fieldId), dto).andExpect(status().isCreated());

    tryPost(fieldSubfieldsPath(fieldId), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(DataIntegrityViolationException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.DUPLICATE_SUBFIELD.getType())))
      .andExpect(errorMessageMatch(is("The 'code' must be unique.")));
  }

  @Test
  void createFieldLocalSubfield_shouldReturn400_whenControlField() throws Exception {
    var fieldId = createLocalField("004");
    var dto = localTestSubfield("a", "Subfield a");

    tryPost(fieldSubfieldsPath(fieldId), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(ResourceValidationFailedException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.CONTROL_FIELD_RESOURCE_NOT_ALLOWED.getType())))
      .andExpect(errorMessageMatch(is("Cannot define subfields for 00X control fields.")));
  }

  private Object getRecordId(MvcResult result) throws UnsupportedEncodingException {
    return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
  }
}
