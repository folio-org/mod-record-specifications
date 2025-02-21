package org.folio.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.fieldSubfieldsPath;
import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.ApiEndpoints.specificationPath;
import static org.folio.support.ApiEndpoints.specificationRulePath;
import static org.folio.support.ApiEndpoints.specificationRulesPath;
import static org.folio.support.ApiEndpoints.specificationsPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.folio.support.builders.FieldBuilder.local;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.QueryParams;
import org.folio.support.SpecificationITBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@IntegrationTest
@DatabaseCleanup(tables = FIELD_TABLE_NAME, tenants = TENANT_ID)
class SpecificationStorageApiIT extends SpecificationITBase {

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
      .addQueryParam("profile", "bibliographic")
      .addQueryParam("include", "all");

    var result = doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), local().buildChangeDto()).andReturn();
    var createdFieldId = JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
    doPost(fieldSubfieldsPath(createdFieldId), """
      {
      "code": "a",
      "label": "Subfield a"
      }
      """);
    var indicatorResult = doPost(fieldIndicatorsPath(createdFieldId), """
      {
      "order": "1",
      "label": "Indicator 1"
      }
      """).andReturn();
    var createdIndicatorId = JsonPath.read(indicatorResult.getResponse().getContentAsString(), "$.id").toString();
    doPost(indicatorCodesPath(createdIndicatorId), """
      {
      "code": "a",
      "label": "Subfield a"
      }
      """);

    doGet(specificationsPath(queryParams))
      .andExpect(jsonPath("totalRecords", is(1)))
      .andExpect(jsonPath("specifications.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].id", notNullValue()))
      .andExpect(jsonPath("specifications.[0].title", notNullValue()))
      .andExpect(jsonPath("specifications.[0].family", notNullValue()))
      .andExpect(jsonPath("specifications.[0].profile", notNullValue()))
      .andExpect(jsonPath("specifications.[0].url", notNullValue()))
      .andExpect(jsonPath("specifications.[0].fields.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].fields.[0].subfields.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].fields.[0].indicators.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].fields.[0].indicators.[0].codes.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].rules.size()", greaterThan(1)));
  }

  @Test
  void getSpecifications_shouldReturn200AndCollectionWithRequiredFields() throws Exception {
    var queryParams = new QueryParams()
      .addQueryParam("family", "MARC")
      .addQueryParam("profile", "bibliographic")
      .addQueryParam("include", "fields.required");

    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), local().buildChangeDto());
    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID),
      local().tag("101").required(false).buildChangeDto());

    doGet(specificationsPath(queryParams))
      .andExpect(jsonPath("totalRecords", is(1)))
      .andExpect(jsonPath("specifications.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].id", notNullValue()))
      .andExpect(jsonPath("specifications.[0].title", notNullValue()))
      .andExpect(jsonPath("specifications.[0].family", notNullValue()))
      .andExpect(jsonPath("specifications.[0].profile", notNullValue()))
      .andExpect(jsonPath("specifications.[0].url", notNullValue()))
      .andExpect(jsonPath("specifications.[0].fields.size()", is(1)))
      .andExpect(jsonPath("specifications.[0].rules").doesNotExist());
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
  void getSpecification_shouldReturn200AndSpecificationWithBaseFields() throws Exception {
    doGet(specificationPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("title", notNullValue()))
      .andExpect(jsonPath("family", notNullValue()))
      .andExpect(jsonPath("profile", notNullValue()))
      .andExpect(jsonPath("url", notNullValue()))
      .andExpect(jsonPath("metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("metadata.updatedDate", notNullValue()));
  }

  @Test
  void getSpecification_shouldReturn200AndSpecificationWithAllFields() throws Exception {
    var queryParams = new QueryParams()
      .addQueryParam("include", "all");

    var result = doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), local().buildChangeDto()).andReturn();
    var createdFieldId = JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
    doPost(fieldSubfieldsPath(createdFieldId), """
      {
      "code": "a",
      "label": "Subfield a"
      }
      """);
    var indicatorResult = doPost(fieldIndicatorsPath(createdFieldId), """
      {
      "order": "1",
      "label": "Indicator 1"
      }
      """).andReturn();
    var createdIndicatorId = JsonPath.read(indicatorResult.getResponse().getContentAsString(), "$.id").toString();
    doPost(indicatorCodesPath(createdIndicatorId), """
      {
      "code": "a",
      "label": "Subfield a"
      }
      """);

    doGet(specificationPath(BIBLIOGRAPHIC_SPECIFICATION_ID, queryParams))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("title", notNullValue()))
      .andExpect(jsonPath("family", notNullValue()))
      .andExpect(jsonPath("profile", notNullValue()))
      .andExpect(jsonPath("url", notNullValue()))
      .andExpect(jsonPath("fields.size()", is(1)))
      .andExpect(jsonPath("fields.[0].subfields.size()", is(1)))
      .andExpect(jsonPath("fields.[0].indicators.size()", is(1)))
      .andExpect(jsonPath("fields.[0].indicators.[0].codes.size()", is(1)))
      .andExpect(jsonPath("rules.size()", greaterThan(1)));
  }

  @Test
  void getSpecification_shouldReturn200AndSpecificationWithRequiredFields() throws Exception {
    var queryParams = new QueryParams()
      .addQueryParam("include", "fields.required");

    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), local().buildChangeDto());
    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID),
      local().tag("101").required(false).buildChangeDto());

    doGet(specificationPath(BIBLIOGRAPHIC_SPECIFICATION_ID, queryParams))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("title", notNullValue()))
      .andExpect(jsonPath("family", notNullValue()))
      .andExpect(jsonPath("profile", notNullValue()))
      .andExpect(jsonPath("url", notNullValue()))
      .andExpect(jsonPath("fields.size()", is(1)))
      .andExpect(jsonPath("rules").doesNotExist());
  }

  @Test
  void getSpecificationRules_shouldReturn200AndCollectionOfRules() throws Exception {
    doGet(specificationRulesPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("totalRecords", is(15)))
      .andExpect(jsonPath("rules.size()", is(15)))
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
      .andExpect(errorMessageMatch(is("specification with ID [%s] was not found.".formatted(notExistId))));
  }

  @Test
  void toggleSpecificationRule_shouldReturn204AndToggleSpecificationRule() {
    var specificationRules = getSpecificationRules(BIBLIOGRAPHIC_SPECIFICATION_ID);
    var specificationRuleToToggle = specificationRules.getRules().getFirst();
    var stateBeforeToggle = specificationRuleToToggle.getEnabled();

    var specificationRuleId = specificationRuleToToggle.getId();

    var stateAfterToggle = Boolean.FALSE.equals(stateBeforeToggle);
    var toggleDto = new ToggleSpecificationRuleDto(stateAfterToggle);
    doPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, specificationRuleId), toggleDto);
    assertSpecificationRuleEnabled(specificationRuleId, BIBLIOGRAPHIC_SPECIFICATION_ID, stateAfterToggle);

    toggleDto = toggleDto.enabled(stateBeforeToggle);
    doPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, specificationRuleId), toggleDto);
    assertSpecificationRuleEnabled(specificationRuleId, BIBLIOGRAPHIC_SPECIFICATION_ID,
      Boolean.TRUE.equals(stateBeforeToggle));

    assertSpecificationUpdatedEvents(2);
  }

  @Test
  void toggleSpecificationRule_shouldReturn404WhenSpecificationRuleNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, notExistId), new ToggleSpecificationRuleDto(false))
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("specification rule with ID [specificationId=%s, ruleId=%s] was not found."
        .formatted(BIBLIOGRAPHIC_SPECIFICATION_ID, notExistId))));
  }

  @Test
  void toggleSpecificationRule_shouldReturn400WhenIncompleteBody() throws Exception {
    tryPatch(specificationRulePath(BIBLIOGRAPHIC_SPECIFICATION_ID, UUID.randomUUID()),
      new ToggleSpecificationRuleDto())
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(MethodArgumentNotValidException.class))
      .andExpect(errorMessageMatch(is("The 'enabled' field is required.")))
      .andExpect(errorParameterMatch("enabled"));
  }

  @Test
  void getSpecificationFields_shouldReturn200AndCollectionOfFields() throws Exception {
    var dto1 = localTestField("101");
    var dto2 = localTestField("102");
    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto1);
    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto2);

    doGet(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID))
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("fields.size()", is(2)))
      .andExpect(jsonPath("fields.[*].id", everyItem(notNullValue())))
      .andExpect(jsonPath("fields.[*].tag", hasItems(dto1.getTag(), dto2.getTag())))
      .andExpect(jsonPath("fields.[*].label", hasItems(dto1.getLabel(), dto2.getLabel())))
      .andExpect(jsonPath("fields.[*].specificationId", everyItem(is(BIBLIOGRAPHIC_SPECIFICATION_ID.toString()))))
      .andExpect(jsonPath("fields.[*].url", hasItems(dto1.getUrl(), dto2.getUrl())))
      .andExpect(jsonPath("fields.[*].repeatable", hasItems(dto1.getRepeatable(), dto2.getRepeatable())))
      .andExpect(jsonPath("fields.[*].required", hasItems(dto1.getRequired(), dto2.getRequired())))
      .andExpect(jsonPath("fields.[*].deprecated", hasItems(dto1.getDeprecated(), dto2.getDeprecated())))
      .andExpect(jsonPath("fields.[*].scope", everyItem(is(Scope.LOCAL.getValue()))))
      .andExpect(jsonPath("fields.[*].metadata.createdDate", everyItem(notNullValue())))
      .andExpect(jsonPath("fields.[*].metadata.createdByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("fields.[*].metadata.updatedByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("fields.[*].metadata.updatedDate", everyItem(notNullValue())));
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

    assertSpecificationUpdatedEvent();
  }

  @Test
  void createSpecificationLocalField_shouldReturn400WhenFieldForTagAlreadyExist() throws Exception {
    var dto = localTestField("998");

    doPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto);

    tryPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(DataIntegrityViolationException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.DUPLICATE_FIELD_TAG.getType())))
      .andExpect(errorMessageMatch(is("The 'tag' must be unique.")));
  }

  @Test
  void createSpecificationLocalField_shouldReturn400WhenFieldTagIsNotAlphabetical() throws Exception {
    var dto = localTestField("abc");

    tryPost(specificationFieldsPath(BIBLIOGRAPHIC_SPECIFICATION_ID), dto)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(MethodArgumentNotValidException.class))
      .andExpect(errorMessageMatch(
        is("A 'tag' field must contain three characters and can only accept numbers 0-9.")))
      .andExpect(errorTypeMatch(is(ErrorCode.INVALID_REQUEST_PARAMETER.getType())))
      .andExpect(errorParameterMatch("tag"));
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
