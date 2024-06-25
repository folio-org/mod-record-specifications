package org.folio.api;

import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.rspec.domain.entity.Indicator.INDICATOR_TABLE_NAME;
import static org.folio.rspec.domain.entity.IndicatorCode.INDICATOR_CODE_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.Scope;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

@IntegrationTest
@DatabaseCleanup(tables = {INDICATOR_CODE_TABLE_NAME, INDICATOR_TABLE_NAME, FIELD_TABLE_NAME}, tenants = TENANT_ID)
class SpecificationStorageIndicatorsApiIT extends IntegrationTestBase {

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void getIndicatorCodes_shouldReturn200AndAllCodesForIndicator() throws Exception {
    var fieldId = createLocalField("101");
    var indId = createLocalIndicator(fieldId);
    var code1 = localTestCode("a");
    var code2 = localTestCode("b");
    doPost(indicatorCodesPath(indId), code1);
    doPost(indicatorCodesPath(indId), code2);

    doGet(indicatorCodesPath(indId))
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("codes.size()", is(2)))
      .andExpect(jsonPath("codes.[*].id", everyItem(notNullValue())))
      .andExpect(jsonPath("codes.[*].indicatorId", everyItem(is(indId))))
      .andExpect(jsonPath("codes.[*].code", hasItems(code1.getCode(), code2.getCode())))
      .andExpect(jsonPath("codes.[*].label", hasItems(code1.getLabel(), code2.getLabel())))
      .andExpect(jsonPath("codes.[*].scope", everyItem(is(Scope.LOCAL.getValue()))))
      .andExpect(jsonPath("codes.[*].metadata.createdDate", everyItem(notNullValue())))
      .andExpect(jsonPath("codes.[*].metadata.createdByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("codes.[*].metadata.updatedByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("codes.[*].metadata.updatedDate", everyItem(notNullValue())));
  }

  @Test
  void createIndicatorLocalCode_shouldReturn201AndCreatedCode() throws Exception {
    var fieldId = createLocalField("102");
    var indId = createLocalIndicator(fieldId);
    var code = localTestCode("a");

    var result = doPost(indicatorCodesPath(indId), code)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()))
      .andExpect(jsonPath("$.indicatorId", is(indId)))
      .andExpect(jsonPath("$.code", is(code.getCode())))
      .andExpect(jsonPath("$.label", is(code.getLabel())))
      .andExpect(jsonPath("$.scope", is(Scope.LOCAL.getValue())))
      .andExpect(jsonPath("$.metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.createdByUserId", is(USER_ID)))
      .andExpect(jsonPath("$.metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.updatedByUserId", is(USER_ID)))
      .andReturn();

    var createdCodeId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

    doGet(indicatorCodesPath(indId))
      .andExpect(jsonPath("$.codes.[*].id", hasItem(createdCodeId)));
  }

  @Test
  void createIndicatorLocalCode_shouldReturn400WhenIndicatorCodeAlreadyExist() throws Exception {
    var fieldId = createLocalField("102");
    var indId = createLocalIndicator(fieldId);
    var code = localTestCode("a");

    doPost(indicatorCodesPath(indId), code);

    tryPost(indicatorCodesPath(indId), code)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(DataIntegrityViolationException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.DUPLICATE_INDICATOR_CODE.getType())))
      .andExpect(errorMessageMatch(is("Can only have one validation rule per MARC field indicator/code.")));
  }

  private String createLocalIndicator(String fieldId) {
    var ind = localTestIndicator(1);
    return doPostAndReturn(fieldIndicatorsPath(fieldId), ind, FieldIndicatorDto.class).getId().toString();
  }

  protected IndicatorCodeChangeDto localTestCode(String code) {
    return new IndicatorCodeChangeDto()
      .code(code)
      .label(easyRandom.nextObject(String.class));
  }

}
