package org.folio.rspec.controller;

import static org.folio.support.ApiEndpoints.specificationRulePath;
import static org.folio.support.ApiEndpoints.specificationRulesPath;
import static org.folio.support.ApiEndpoints.specificationsPath;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.service.SpecificationService;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageController.class)
class SpecificationStorageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SpecificationService specificationService;

  @Test
  void getSpecifications_returnSpecifications(@Random SpecificationDto specificationDto) throws Exception {
    when(specificationService.findSpecifications(null, null, IncludeParam.NONE, 100, 0))
      .thenReturn(new SpecificationDtoCollection().totalRecords(1).addSpecificationsItem(specificationDto));

    var requestBuilder = get(specificationsPath())
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(1)))
      .andExpect(jsonPath("$.specifications.size()", is(1)))
      .andExpect(jsonPath("$.specifications[0].id", is(specificationDto.getId().toString())))
      .andExpect(jsonPath("$.specifications[0].title", is(specificationDto.getTitle())))
      .andExpect(jsonPath("$.specifications[0].family", is(specificationDto.getFamily().getValue())))
      .andExpect(jsonPath("$.specifications[0].profile", is(specificationDto.getProfile().getValue())));

    verify(specificationService).findSpecifications(null, null, IncludeParam.NONE, 100, 0);
  }

  @Test
  void getSpecifications_notImplemented_whenIncludeValueIsNotSupported() throws Exception {
    var requestBuilder = get(specificationsPath())
      .queryParam("include", "required-fields")
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotImplemented());

    verifyNoInteractions(specificationService);
  }

  @Test
  void getSpecificationRules_returnSpecificationRules(@Random SpecificationRuleDto ruleDto) throws Exception {
    var specificationId = UUID.randomUUID();
    when(specificationService.findSpecificationRules(specificationId))
      .thenReturn(new SpecificationRuleDtoCollection().totalRecords(1).addRulesItem(ruleDto));

    var requestBuilder = get(specificationRulesPath(specificationId))
      .accept(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(1)))
      .andExpect(jsonPath("$.rules.size()", is(1)))
      .andExpect(jsonPath("$.rules[0].id", is(ruleDto.getId().toString())))
      .andExpect(jsonPath("$.rules[0].name", is(ruleDto.getName())))
      .andExpect(jsonPath("$.rules[0].description", is(ruleDto.getDescription())))
      .andExpect(jsonPath("$.rules[0].code", is(ruleDto.getCode())));
  }

  @Test
  void toggleSpecificationRule_doPartialUpdate() throws Exception {
    var specificationId = UUID.randomUUID();
    var ruleId = UUID.randomUUID();
    doNothing().when(specificationService)
      .toggleSpecificationRule(specificationId, ruleId, new ToggleSpecificationRuleDto(Boolean.FALSE));

    var requestBuilder = patch(specificationRulePath(specificationId, ruleId))
      .contentType(APPLICATION_JSON)
      .content("{\"enabled\": false}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNoContent());

    verify(specificationService).toggleSpecificationRule(specificationId, ruleId,
      new ToggleSpecificationRuleDto(Boolean.FALSE));
  }
}
