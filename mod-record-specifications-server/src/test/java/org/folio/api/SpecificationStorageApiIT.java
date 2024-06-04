package org.folio.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.ApiEndpoints.specificationRulePath;
import static org.folio.support.ApiEndpoints.specificationRulesPath;
import static org.folio.support.ApiEndpoints.specificationsPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.folio.support.QueryParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@IntegrationTest
class SpecificationStorageApiIT extends IntegrationTestBase {

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @Test
  void getSpecifications_shouldReturn200AndCollectionWithBaseFields_noFilters() throws Exception {
    doGet(specificationsPath())
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("specifications.size()", is(2)))
      .andExpect(jsonPath("specifications.[0].id", notNullValue()))
      .andExpect(jsonPath("specifications.[0].title", notNullValue()))
      .andExpect(jsonPath("specifications.[0].family", notNullValue()))
      .andExpect(jsonPath("specifications.[0].profile", notNullValue()))
      .andExpect(jsonPath("specifications.[0].url", notNullValue()))
      .andExpect(jsonPath("specifications.[0].metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("specifications.[0].metadata.updatedDate", notNullValue()));
  }

  @Test
  void getSpecifications_shouldReturn200AndCollectionWithAllFields() throws Exception {
    var queryParams = new QueryParams()
      .addQueryParam("family", "MARC")
      .addQueryParam("profile", "authority")
      .addQueryParam("include", "all");

    doGet(specificationsPath(queryParams))
      .andExpect(jsonPath("totalRecords", is(1)))
      .andExpect(jsonPath("specifications.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].id", notNullValue()))
      .andExpect(jsonPath("specifications.[0].title", notNullValue()))
      .andExpect(jsonPath("specifications.[0].family", notNullValue()))
      .andExpect(jsonPath("specifications.[0].profile", notNullValue()))
      .andExpect(jsonPath("specifications.[0].url", notNullValue()))
      .andExpect(jsonPath("specifications.[0].metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("specifications.[0].metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("specifications.[0].rules.size()", greaterThan(1)));
  }

  @Test
  void getSpecifications_shouldReturn200AndCollection_allFilters() throws Exception {
    var queryParams = new QueryParams().addQueryParam("family", "MARC").addQueryParam("profile", "authority");

    doGet(specificationsPath(queryParams))
      .andExpect(jsonPath("totalRecords", is(1)))
      .andExpect(jsonPath("specifications.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].family", is("MARC")))
      .andExpect(jsonPath("specifications.[0].profile", is("authority")));
  }

  @Test
  void getSpecifications_shouldReturn200AndCollection_familyFilterAndPagination() throws Exception {
    var queryParams = new QueryParams().addQueryParam("family", "MARC")
      .addQueryParam("limit", "1")
      .addQueryParam("offset", "1");

    doGet(specificationsPath(queryParams))
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("specifications.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].family", is("MARC")))
      .andExpect(jsonPath("specifications.[0].profile", is("authority")));
  }

  @Test
  void getSpecificationRules_shouldReturn200AndCollectionOfRules() throws Exception {
    doGet(specificationRulesPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("totalRecords", is(12)))
      .andExpect(jsonPath("rules.size()", is(12)))
      .andExpect(jsonPath("rules[0].id", notNullValue()))
      .andExpect(jsonPath("rules[0].name", notNullValue()))
      .andExpect(jsonPath("rules[0].description", notNullValue()))
      .andExpect(jsonPath("rules[0].code", notNullValue()))
      .andExpect(jsonPath("rules[0].enabled", notNullValue()))
      .andExpect(jsonPath("rules[0].specificationId", is(BIBLIOGRAPHIC_SPECIFICATION_ID.toString())))
      .andExpect(jsonPath("rules[0].metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("rules[0].metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("rules[0].metadata.createdByUserId", notNullValue()))
      .andExpect(jsonPath("rules[0].metadata.updatedByUserId", notNullValue()));
  }

  @Test
  void getSpecificationRules_shouldReturn404WhenSpecificationNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryGet(specificationRulesPath(notExistId))
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("specification with ID [%s] was not found".formatted(notExistId))));
  }

  @Test
  void toggleSpecificationRule_shouldReturn204AndToggleSpecificationRule() {
    var specificationRules = getSpecificationRules(BIBLIOGRAPHIC_SPECIFICATION_ID);
    var specificationRuleToToggle = specificationRules.getRules().get(0);
    var stateBeforeToggle = specificationRuleToToggle.getEnabled();

    var specificationRuleId = specificationRuleToToggle.getId();

    var stateAfterToggle = !stateBeforeToggle;
    var toggleDto = new ToggleSpecificationRuleDto(stateAfterToggle);
    doPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, specificationRuleId), toggleDto);
    assertSpecificationRuleEnabled(specificationRuleId, BIBLIOGRAPHIC_SPECIFICATION_ID, stateAfterToggle);

    toggleDto = toggleDto.enabled(stateBeforeToggle);
    doPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, specificationRuleId), toggleDto);
    assertSpecificationRuleEnabled(specificationRuleId, BIBLIOGRAPHIC_SPECIFICATION_ID, stateBeforeToggle);
  }

  @Test
  void toggleSpecificationRule_shouldReturn404WhenSpecificationRuleNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, notExistId), new ToggleSpecificationRuleDto(false))
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("specification rule with ID [specificationId=%s, ruleId=%s] was not found"
        .formatted(BIBLIOGRAPHIC_SPECIFICATION_ID, notExistId))));
  }

  @Test
  void toggleSpecificationRule_shouldReturn400WhenIncompleteBody() throws Exception {
    tryPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, UUID.randomUUID()),
      new ToggleSpecificationRuleDto())
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(MethodArgumentNotValidException.class))
      .andExpect(errorMessageMatch(is("Field [enabled] must be not null.")))
      .andExpect(errorParameterMatch("enabled"));
  }

  @Test
  void getSpecificationFields_shouldReturn200AndCollectionOfFields() throws Exception {
    doGet(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("totalRecords", greaterThan(1)))
      .andExpect(jsonPath("fields.size()", greaterThan(1)))
      .andExpect(jsonPath("fields.[0].id", notNullValue()))
      .andExpect(jsonPath("fields.[0].tag", notNullValue()))
      .andExpect(jsonPath("fields.[0].label", notNullValue()))
      .andExpect(jsonPath("fields.[0].specificationId", is(BIBLIOGRAPHIC_SPECIFICATION_ID.toString())))
      .andExpect(jsonPath("fields.[0].url", notNullValue()))
      .andExpect(jsonPath("fields.[0].repeatable", notNullValue()))
      .andExpect(jsonPath("fields.[0].required", notNullValue()))
      .andExpect(jsonPath("fields.[0].deprecated", notNullValue()))
      .andExpect(jsonPath("fields.[0].scope", notNullValue()))
      .andExpect(jsonPath("fields.[0].metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("fields.[0].metadata.createdByUserId", notNullValue()))
      .andExpect(jsonPath("fields.[0].metadata.updatedByUserId", notNullValue()))
      .andExpect(jsonPath("fields.[0].metadata.updatedDate", notNullValue()));
  }

  @Test
  void createSpecificationLocalField_shouldReturn201AndCreatedField() throws Exception {
    var dto = localTestField("666");

    var result = doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()))
      .andExpect(jsonPath("$.scope", is(Scope.LOCAL.getValue())))
      .andExpect(jsonPath("$.specificationId", is(BIBLIOGRAPHIC_SPECIFICATION_ID.toString())))
      .andExpect(jsonPath("$.tag", is(dto.getTag())))
      .andExpect(jsonPath("$.label", is(dto.getLabel())))
      .andExpect(jsonPath("$.deprecated", is(dto.getDeprecated())))
      .andExpect(jsonPath("$.repeatable", is(dto.getRepeatable())))
      .andExpect(jsonPath("$.required", is(dto.getRequired())))
      .andExpect(jsonPath("$.url", is(dto.getUrl())))
      .andExpect(jsonPath("$.metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.createdByUserId", is(USER_ID)))
      .andExpect(jsonPath("$.metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.updatedByUserId", is(USER_ID)))
      .andReturn();

    var createdFieldId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

    doGet(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("$.fields.[*].id", hasItem(createdFieldId)));
  }

  @Test
  void createSpecificationLocalField_shouldReturn400WhenFieldForTagAlreadyExist() throws Exception {
    var dto = localTestField("998");

    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto);

    tryPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(DataIntegrityViolationException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.DUPLICATE_SPECIFICATION_FIELD.getType())))
      .andExpect(errorMessageMatch(is("Can only have one validation rule per MARC field/tag number.")));
  }

  @Test
  void createSpecificationLocalField_shouldReturn400WhenFieldTagIsNotAlphabetical() throws Exception {
    var dto = localTestField("666").tag("abc");

    tryPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(MethodArgumentNotValidException.class))
      .andExpect(errorMessageMatch(is("A MARC tag must contain three characters.")))
      .andExpect(errorTypeMatch(is(ErrorCode.INVALID_REQUEST_PARAMETER.getType())))
      .andExpect(errorParameterMatch("tag"));
  }

  private SpecificationFieldChangeDto localTestField(String tag) {
    return new SpecificationFieldChangeDto()
      .tag(tag)
      .label("Mystic Field")
      .deprecated(true)
      .repeatable(false)
      .required(true)
      .url("http://www.inceptos.com");
  }

  private SpecificationRuleDtoCollection getSpecificationRules(UUID specificationId) {
    return contentAsObj(
      doGet(specificationRulesPath(specificationId)).andReturn(),
      SpecificationRuleDtoCollection.class
    );
  }

  private void assertSpecificationRuleEnabled(UUID ruleId, UUID specificationId, boolean expected) {
    var specificationRulesAfterUpdate = getSpecificationRules(specificationId);
    for (SpecificationRuleDto rule : specificationRulesAfterUpdate.getRules()) {
      if (rule.getId().equals(ruleId)) {
        assertThat(rule.getEnabled()).isEqualTo(expected);
      }
    }
  }
}
