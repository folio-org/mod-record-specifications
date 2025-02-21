package org.folio.rspec.controller;

import static java.util.UUID.randomUUID;
import static org.folio.support.ApiEndpoints.subfieldPath;
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
import org.folio.rspec.config.ValidationConfig;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.SubfieldService;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageSubfieldsController.class)
@Import({ApiExceptionHandler.class, TranslationConfig.class, ValidationConfig.class})
@ComponentScan(basePackages = {"org.folio.rspec.controller.handler",
                               "org.folio.rspec.service.i18n",
                               "org.folio.spring.i18n"})
class SpecificationStorageSubfieldsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SubfieldService subfieldService;

  @Test
  void deleteSubfield_returnNoContent() throws Exception {
    var id = randomUUID();
    doNothing().when(subfieldService).deleteSubfield(id);

    mockMvc.perform(delete(subfieldPath(id)).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  @Test
  void updateSubfield_returnAccepted() throws Exception {
    UUID id = randomUUID();
    SubfieldDto subfieldDto = new SubfieldDto();

    when(subfieldService.updateSubfield(eq(id), any(SubfieldChangeDto.class))).thenReturn(subfieldDto);

    mockMvc.perform(put(subfieldPath(id))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "code": "a",
            "label": "Custom Subfield",
            "repeatable": true,
            "required": true,
            "deprecated": true
          }
          """))
      .andExpect(status().isAccepted());
  }

  @ValueSource(strings = {
    "@",
    "a1",
    "/",
    "1234"
  })
  @NullAndEmptySource
  @ParameterizedTest
  void updateSubfield_return400_invalidCode(String codeValue) throws Exception {
    var requestBuilder = put(subfieldPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"" + codeValue + "\", \"label\": \"Mystic subfield\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("A 'code' field must contain one character and can only accept numbers 0-9 or letters a-z."))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Mystic field\"}  | code",
    "{\"code\": \"a\"}              | label"
  })
  @ParameterizedTest
  void updateSubfield_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = put(subfieldPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @Test
  void updateField_return404_notExistedField() throws Exception {
    var fieldId = UUID.randomUUID();
    when(subfieldService.updateSubfield(eq(fieldId), any()))
      .thenThrow(ResourceNotFoundException.forSubfield(fieldId));

    var requestBuilder = put(subfieldPath(fieldId))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"1\", \"label\": \"Mystic subfield\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("subfield with ID [%s] was not found."
        .formatted(fieldId)))));
  }

}
