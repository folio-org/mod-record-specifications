package org.folio.api;

import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.folio.support.TestConstants;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@IntegrationTest
@DatabaseCleanup(tables = FIELD_TABLE_NAME, tenants = TENANT_ID)
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
      .andExpect(errorMessageMatch(is("field definition with ID [%s] was not found".formatted(notExistId))));
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
      .andExpect(errorMessageMatch(is("field definition with ID [%s] was not found".formatted(notExistId))));
  }

  private SpecificationFieldChangeDto getLocalFieldDto() {
    return new SpecificationFieldChangeDto()
      .tag("998")
      .required(true)
      .deprecated(false)
      .required(false)
      .label("Local Test Field");
  }

  private String createLocalField(SpecificationFieldChangeDto localTestField) throws UnsupportedEncodingException {
    return JsonPath.read(
      doPost(specificationFieldsPath(TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID), localTestField)
        .andReturn()
        .getResponse().getContentAsString(),
      "$.id").toString();
  }

}
