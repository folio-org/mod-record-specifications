package org.folio.rspec.controller;

import static java.util.UUID.randomUUID;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.fieldPath;
import static org.folio.support.ApiEndpoints.fieldSubfieldsPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsString;
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
import org.folio.rspec.config.ValidationConfig;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.SubfieldDtoCollection;
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
@Import({ApiExceptionHandler.class, TranslationConfig.class, ValidationConfig.class})
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

  @Test
  void updateField_return400_invalidJson() throws Exception {
    var requestBuilder = put(fieldPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"666\", \"label\": \"Mystic field\", \"deprecated\": invalid}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(containsString("JSON parse error"))));
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
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("A 'tag' field must contain three characters and can only accept numbers 0-9."))));
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

  @Test
  void getFieldIndicators(@Random FieldIndicatorDto indicatorDto) throws Exception {
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
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "3"})
  void createFieldLocalIndicator_return400_invalidOrder(String order) throws Exception {
    var requestBuilder = post(fieldIndicatorsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": %s, \"label\": \"Ind\"}".formatted(order));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The indicator 'order' field can only accept numbers 1-2."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("order"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(order))));
  }

  @Test
  void createFieldLocalIndicator_return400_blankLabel() throws Exception {
    var requestBuilder = post(fieldIndicatorsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 1, \"label\": \"\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.size()", is(1)))
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field is required."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("label"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(""))));
  }

  @Test
  void createFieldLocalIndicator_return400_longLabel() throws Exception {
    var label = "a".repeat(351);
    var requestBuilder = post(fieldIndicatorsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 1, \"label\": \"%s\"}".formatted(label));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field has exceeded 350 character limit."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("label"))));
  }

  @Test
  void getFieldSubfields(@Random SubfieldDto subfieldDto) throws Exception {
    var fieldId = UUID.randomUUID();
    when(specificationFieldService.findFieldSubfields(fieldId))
      .thenReturn(new SubfieldDtoCollection().totalRecords(1).addSubfieldsItem(subfieldDto));

    var requestBuilder = get(fieldSubfieldsPath(fieldId))
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(1)))
      .andExpect(jsonPath("$.subfields.size()", is(1)))
      .andExpect(jsonPath("$.subfields[0].id", is(subfieldDto.getId().toString())))
      .andExpect(jsonPath("$.subfields[0].fieldId", is(subfieldDto.getFieldId().toString())))
      .andExpect(jsonPath("$.subfields[0].code", is(subfieldDto.getCode())))
      .andExpect(jsonPath("$.subfields[0].label", is(subfieldDto.getLabel())))
      .andExpect(jsonPath("$.subfields[0].required", is(subfieldDto.getRequired())))
      .andExpect(jsonPath("$.subfields[0].repeatable", is(subfieldDto.getRepeatable())))
      .andExpect(jsonPath("$.subfields[0].deprecated", is(subfieldDto.getDeprecated())))
      .andExpect(jsonPath("$.subfields[0].scope", is(subfieldDto.getScope().getValue())));
  }

  @Test
  void createFieldLocalSubfield_createNewLocalSubfield(@Random SubfieldDto subfieldDto) throws Exception {
    var fieldId = UUID.randomUUID();
    when(specificationFieldService.createLocalSubfield(eq(fieldId), any())).thenReturn(subfieldDto);

    var requestBuilder = post(fieldSubfieldsPath(fieldId))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"a\", \"label\": \"subfield a\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", is(subfieldDto.getId().toString())))
      .andExpect(jsonPath("$.fieldId", is(subfieldDto.getFieldId().toString())))
      .andExpect(jsonPath("$.code", is(subfieldDto.getCode())))
      .andExpect(jsonPath("$.label", is(subfieldDto.getLabel())))
      .andExpect(jsonPath("$.required", is(subfieldDto.getRequired())))
      .andExpect(jsonPath("$.repeatable", is(subfieldDto.getRepeatable())))
      .andExpect(jsonPath("$.deprecated", is(subfieldDto.getDeprecated())))
      .andExpect(jsonPath("$.scope", is(subfieldDto.getScope().getValue())));
  }

  @Test
  void createFieldLocalSubfield_return404_notExistingField() throws Exception {
    var fieldId = UUID.randomUUID();
    when(specificationFieldService.createLocalSubfield(eq(fieldId), any()))
      .thenThrow(ResourceNotFoundException.forField(fieldId));

    var requestBuilder = post(fieldSubfieldsPath(fieldId))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"a\", \"label\": \"Ind 1\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("field definition with ID [%s] was not found."
        .formatted(fieldId)))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Subfield 1m\"}  | code",
    "{\"code\": 1}                 | label"
  })
  @ParameterizedTest
  void createFieldLocalSubfield_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = post(fieldSubfieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @ValueSource(strings = {"#", "/", "A", "aa", "ä"})
  @ParameterizedTest
  void createFieldLocalSubfield_return400_invalidCode(String invalidCode) throws Exception {
    var requestBuilder = post(fieldSubfieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"%s\", \"label\": \"Subfield ?\"}".formatted(invalidCode));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("A 'code' field must contain one character and can only accept numbers 0-9 or letters a-z."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("code"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(invalidCode))));
  }

  @Test
  void createFieldLocalSubfield_return400_blankLabel() throws Exception {
    var requestBuilder = post(fieldSubfieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"1\", \"label\": \"\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.size()", is(1)))
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field is required."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("label"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(""))));
  }

  @Test
  void createFieldLocalSubfield_return400_longLabel() throws Exception {
    var label = "a".repeat(351);
    var requestBuilder = post(fieldSubfieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"1\", \"label\": \"%s\"}".formatted(label));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field has exceeded 350 character limit."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("label"))));
  }
}
