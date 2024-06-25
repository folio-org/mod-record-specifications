package org.folio.api;

import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.rspec.domain.entity.Indicator.INDICATOR_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
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
import java.util.Map;
import java.util.UUID;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

@IntegrationTest
@DatabaseCleanup(tables = {INDICATOR_TABLE_NAME, FIELD_TABLE_NAME}, tenants = TENANT_ID)
class SpecificationStorageFieldsApiIT extends IntegrationTestBase {

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void deleteField_shouldReturn204AndDeleteLocalField() throws Exception {
    var createdFieldId = createLocalField(getLocalFieldDto());

    doDelete(fieldPath(createdFieldId));

    doGet(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("$.fields.[*].id", not(hasItem(createdFieldId))));
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
    var localTestField = getLocalFieldDto();
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
  }

  @Test
  void updateField_shouldReturn404WhenFieldNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPut(fieldPath(notExistId), getLocalFieldDto())
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

    var createdIndicatorId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

    doGet(fieldIndicatorsPath(fieldId))
      .andExpect(jsonPath("$.indicators.[*].id", hasItem(createdIndicatorId)));
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
      .andExpect(errorMessageMatch(is("Can only have one validation rule per MARC field indicator/order.")));
  }

  private SpecificationFieldChangeDto getLocalFieldDto() {
    return new SpecificationFieldChangeDto()
      .tag("998")
      .required(true)
      .deprecated(false)
      .required(false)
      .label("Local Test Field");
  }

}
