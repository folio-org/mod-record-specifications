package org.folio.rspec.controller;

import static java.util.UUID.randomUUID;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.config.TranslationConfig;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageFieldsController.class)
@Import({ApiExceptionHandler.class, TranslationConfig.class})
@ComponentScan(basePackages = {"org.folio.rspec.controller.handler",
                               "org.folio.rspec.service.i18n",
                               "org.folio.spring.i18n"})
class SpecificationStorageFieldsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SpecificationFieldService specificationFieldService;

  @Test
  void deleteField_returnNoContent() throws Exception {
    var id = randomUUID();
    doNothing().when(specificationFieldService).deleteField(id);

    mockMvc.perform(delete(fieldPath(id))
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  @Test
  void updateField_returnAccepted() throws Exception {
    UUID id = randomUUID();
    SpecificationFieldDto specificationFieldDto = new SpecificationFieldDto();

    when(specificationFieldService.updateField(eq(id), any(SpecificationFieldChangeDto.class))).thenReturn(
      specificationFieldDto);

    mockMvc.perform(put(fieldPath(id))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "tag": 888,
            "label": "Custom Field - Contributor Data",
            "url": "http://www.example.org/field888.html",
            "repeatable": true,
            "required": true,
            "deprecated": true
          }
          """))
      .andExpect(status().isAccepted());
  }

  @Test
  void updateField_return400_invalidUrl() throws Exception {
    var requestBuilder = put(fieldPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"666\", \"label\": \"Mystic field\", \"url\": \"invalid\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The 'url' field should be valid URL."))));
  }

  @ValueSource(strings = {
    "abc",
    "a1x",
    "1",
    "1234"
  })
  @NullAndEmptySource
  @ParameterizedTest
  void updateField_return400_invalidTag(String tagValue) throws Exception {
    var requestBuilder = put(fieldPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"" + tagValue + "\", \"label\": \"Mystic field\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("A tag must contain three characters."))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Mystic field\"}  | tag",
    "{\"tag\": \"666\"}             | label"
  })
  @ParameterizedTest
  void updateField_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = put(fieldPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @Test
  void updateField_return404_notExistedSpecification() throws Exception {
    var specificationId = UUID.randomUUID();
    when(specificationFieldService.updateField(eq(specificationId), any()))
      .thenThrow(ResourceNotFoundException.forSpecification(specificationId));

    var requestBuilder = put(fieldPath(specificationId))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"666\", \"label\": \"Mystic field\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("specification with ID [%s] was not found."
        .formatted(specificationId)))));
  }

}
