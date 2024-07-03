package org.folio.rspec.controller;

import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.config.TranslationConfig;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDtoCollection;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.FieldIndicatorService;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageIndicatorsController.class)
@Import({ApiExceptionHandler.class, TranslationConfig.class})
@ComponentScan(basePackages = {"org.folio.rspec.controller.handler",
                               "org.folio.rspec.service.i18n",
                               "org.folio.spring.i18n"})
class SpecificationStorageIndicatorsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FieldIndicatorService fieldIndicatorService;

  @Test
  void getIndicatorCodes(@Random IndicatorCodeDto codeDto) throws Exception {
    var indicatorId = UUID.randomUUID();
    when(fieldIndicatorService.findIndicatorCodes(indicatorId))
      .thenReturn(new IndicatorCodeDtoCollection().totalRecords(1).addCodesItem(codeDto));

    var requestBuilder = get(indicatorCodesPath(indicatorId))
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(1)))
      .andExpect(jsonPath("$.codes.size()", is(1)))
      .andExpect(jsonPath("$.codes[0].id", is(codeDto.getId().toString())))
      .andExpect(jsonPath("$.codes[0].indicatorId", is(codeDto.getIndicatorId().toString())))
      .andExpect(jsonPath("$.codes[0].code", is(codeDto.getCode())))
      .andExpect(jsonPath("$.codes[0].label", is(codeDto.getLabel())))
      .andExpect(jsonPath("$.codes[0].scope", is(codeDto.getScope().getValue())));

    verify(fieldIndicatorService).findIndicatorCodes(indicatorId);
  }

  @Test
  void createIndicatorLocalCode_createNewLocalCode(@Random IndicatorCodeDto codeDto) throws Exception {
    var indicatorId = UUID.randomUUID();
    when(fieldIndicatorService.createLocalCode(eq(indicatorId), any())).thenReturn(codeDto);

    var requestBuilder = post(indicatorCodesPath(indicatorId))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"1\", \"label\": \"Some code\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", is(codeDto.getId().toString())))
      .andExpect(jsonPath("$.indicatorId", is(codeDto.getIndicatorId().toString())))
      .andExpect(jsonPath("$.code", is(codeDto.getCode())))
      .andExpect(jsonPath("$.label", is(codeDto.getLabel())))
      .andExpect(jsonPath("$.scope", is(codeDto.getScope().getValue())));
  }

  @Test
  void createIndicatorLocalCode_return404_notExistingIndicator() throws Exception {
    var indicatorId = UUID.randomUUID();
    when(fieldIndicatorService.createLocalCode(eq(indicatorId), any()))
      .thenThrow(ResourceNotFoundException.forIndicator(indicatorId));

    var requestBuilder = post(indicatorCodesPath(indicatorId))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"1\", \"label\": \"Some code\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("field indicator with ID [%s] was not found."
        .formatted(indicatorId)))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Some code\"}  | code",
    "{\"code\": \"a\"}      | label"
  })
  @ParameterizedTest
  void createIndicatorLocalCode_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = post(indicatorCodesPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @ParameterizedTest
  @ValueSource(strings = {"!", "1f", "11"})
  void createIndicatorLocalCode_return400_invalidCode(String code) throws Exception {
    var requestBuilder = post(indicatorCodesPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"%s\", \"label\": \"Some code\"}".formatted(code));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("A 'code' field must contain one character and can only accept "
                   + "numbers 0-9, letters a-z or a '#'."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("code"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(code))));
  }

  @Test
  void createIndicatorLocalCode_return400_blankCode() throws Exception {
    var requestBuilder = post(indicatorCodesPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"\", \"label\": \"Some code\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'code' must be not blank."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("code"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(""))));
  }

  @Test
  void createIndicatorLocalCode_return400_blankLabel() throws Exception {
    var requestBuilder = post(indicatorCodesPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"a\", \"label\": \"\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' must be not blank."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("label"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].value", hasItem(is(""))));
  }

  @Test
  void createIndicatorLocalCode_return400_longLabel() throws Exception {
    var label = "a".repeat(351);
    var requestBuilder = post(indicatorCodesPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"a\", \"label\": \"%s\"}".formatted(label));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field has exceeded 350 character limit."))))
      .andExpect(jsonPath("$.errors.[*].code", hasItem(is("103"))))
      .andExpect(jsonPath("$.errors.[*].parameters.[*].key", hasItem(is("label"))));
  }

}
