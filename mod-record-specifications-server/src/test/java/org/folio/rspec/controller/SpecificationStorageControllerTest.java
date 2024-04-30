package org.folio.rspec.controller;

import static org.folio.support.ApiEndpoints.specificationsPath;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.service.SpecificationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@WebMvcTest(SpecificationStorageController.class)
class SpecificationStorageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SpecificationService specificationService;

  @Test
  void getSpecifications_returnSpecifications() throws Exception {
    when(specificationService.findSpecifications(null, null, null, 100, 0))
      .thenReturn(new SpecificationDtoCollection().totalRecords(0));

    var requestBuilder = get(specificationsPath())
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(0)))
      .andExpect(jsonPath("$.specifications.size()", is(0)));

    verify(specificationService).findSpecifications(null, null, null, 100, 0);
  }

  @Test
  void getSpecifications_notImplemented_whenIncludeIsExists() throws Exception {
    var requestBuilder = get(specificationsPath())
      .queryParam("include", "all")
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotImplemented());

    verifyNoInteractions(specificationService);
  }

}
