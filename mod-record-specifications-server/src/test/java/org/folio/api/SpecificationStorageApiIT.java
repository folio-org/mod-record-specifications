package org.folio.api;

import static org.folio.support.ApiEndpoints.specificationsPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.folio.support.QueryParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
}
