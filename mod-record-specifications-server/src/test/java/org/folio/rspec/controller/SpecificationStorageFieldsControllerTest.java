package org.folio.rspec.controller;

import static java.util.UUID.randomUUID;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.config.TranslationConfig;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.SpecificationFieldService;
import org.folio.spring.testing.extension.Random;
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
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("Field [url] contains invalid URL."))));
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
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("A MARC tag must contain three characters."))));
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
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("Field [%s] must be not null.".formatted(field)))));
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

  @Test
  void getIndicators(@Random FieldIndicatorDto indicatorDto) throws Exception {
    var fieldId = UUID.randomUUID();
    when(specificationFieldService.findFieldIndicators(fieldId))
      .thenReturn(new FieldIndicatorDtoCollection().totalRecords(1).addIndicatorsItem(indicatorDto));

    var requestBuilder = get(fieldIndicatorsPath(fieldId))
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(1)))
      .andExpect(jsonPath("$.indicators.size()", is(1)))
      .andExpect(jsonPath("$.indicators[0].id", is(indicatorDto.getId().toString())))
      .andExpect(jsonPath("$.indicators[0].fieldId", is(indicatorDto.getFieldId().toString())))
      .andExpect(jsonPath("$.indicators[0].order", is(indicatorDto.getOrder())))
      .andExpect(jsonPath("$.indicators[0].label", is(indicatorDto.getLabel())));

    verify(specificationFieldService).findFieldIndicators(fieldId);
  }

  @Test
  void createFieldLocalIndicator_createNewLocalIndicator(@Random FieldIndicatorDto indicatorDto) throws Exception {
    var fieldId = UUID.randomUUID();
    when(specificationFieldService.createLocalIndicator(eq(fieldId), any())).thenReturn(indicatorDto);

    var requestBuilder = post(fieldIndicatorsPath(fieldId))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 1, \"label\": \"Ind 1\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", is(indicatorDto.getId().toString())))
      .andExpect(jsonPath("$.fieldId", is(indicatorDto.getFieldId().toString())))
      .andExpect(jsonPath("$.label", is(indicatorDto.getLabel())))
      .andExpect(jsonPath("$.order", is(indicatorDto.getOrder())));
  }

  @Test
  void createFieldLocalIndicator_return404_notExistingField() throws Exception {
    var fieldId = UUID.randomUUID();
    when(specificationFieldService.createLocalIndicator(eq(fieldId), any()))
      .thenThrow(ResourceNotFoundException.forField(fieldId));

    var requestBuilder = post(fieldIndicatorsPath(fieldId))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 1, \"label\": \"Ind 1\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("field definition with ID [%s] was not found."
        .formatted(fieldId)))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Ind 1m\"}  | order",
    "{\"order\": 1}           | label"
  })
  @ParameterizedTest
  void createFieldLocalIndicator_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = post(fieldIndicatorsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("Field [%s] must be not null.".formatted(field)))));
  }

  @Test
  void createFieldLocalIndicator_return400_highOrder() throws Exception {
    var requestBuilder = post(fieldIndicatorsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 3, \"label\": \"Ind 3\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("order must be less than or equal to 2."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("order"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is("3"))));
  }

  @Test
  void createFieldLocalIndicator_return400_lowOrder() throws Exception {
    var requestBuilder = post(fieldIndicatorsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 0, \"label\": \"Ind 0\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("order must be greater than or equal to 1."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("order"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is("0"))));
  }

}
