package org.folio.rspec.controller;

import static java.util.UUID.randomUUID;
import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.folio.support.ApiEndpoints.indicatorPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.config.TranslationConfig;
import org.folio.rspec.config.ValidationConfig;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDtoCollection;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.FieldIndicatorService;
import org.folio.rspec.service.IndicatorCodeService;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageIndicatorsController.class)
@Import({ApiExceptionHandler.class, TranslationConfig.class, ValidationConfig.class})
@ComponentScan(basePackages = {"org.folio.rspec.controller.handler",
                               "org.folio.rspec.service.i18n",
                               "org.folio.spring.i18n"})
class SpecificationStorageIndicatorsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FieldIndicatorService fieldIndicatorService;

  @MockitoBean
  private IndicatorCodeService indicatorCodeService;

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

  @ParameterizedTest
  @ValueSource(strings = {"#", "5", "c"})
  void createIndicatorLocalCode_createNewLocalCode(String code, @Random IndicatorCodeDto codeDto) throws Exception {
    var indicatorId = UUID.randomUUID();
    codeDto.setCode(code);
    when(fieldIndicatorService.createLocalCode(eq(indicatorId), any())).thenReturn(codeDto);

    var requestBuilder = post(indicatorCodesPath(indicatorId))
      .contentType(APPLICATION_JSON)
      .content("{\"code\": \"%s\", \"label\": \"Some code\"}".formatted(codeDto.getCode()));

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
        hasItem(is("The 'code' field is required."))))
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
      .andExpect(jsonPath("$.errors.size()", is(1)))
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field is required."))))
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

  @Test
  void updateIndicator_returnAccepted() throws Exception {
    var id = randomUUID();
    var fieldIndicatorDto = new FieldIndicatorDto();

    when(fieldIndicatorService.updateIndicator(eq(id), any(FieldIndicatorChangeDto.class))).thenReturn(
      fieldIndicatorDto);

    mockMvc.perform(put(indicatorPath(id))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"order\": 1, \"label\": \"Ind 1\"}"))
      .andExpect(status().isAccepted());
  }

  @Test
  void updateIndicator_return400_invalidJson() throws Exception {
    var requestBuilder = put(indicatorPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": \"a\", \"label\": \"Ind 1\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(containsString("JSON parse error"))));
  }

  @ValueSource(ints = {0, 3})
  @ParameterizedTest
  void updateIndicator_return400_invalidOrder(Integer orderValue) throws Exception {
    var requestBuilder = put(indicatorPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"order\":  %d, \"label\": \"Ind 1\"}".formatted(orderValue));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The indicator 'order' field can only accept numbers 1-2."))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Ind 1\"}  | order",
    "{\"order\": 1}          | label"
  })
  @ParameterizedTest
  void updateIndicator_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = put(indicatorPath(randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @Test
  void updateIndicator_return404_notExistedIndicator() throws Exception {
    var indicatorId = UUID.randomUUID();
    when(fieldIndicatorService.updateIndicator(eq(indicatorId), any()))
      .thenThrow(ResourceNotFoundException.forIndicator(indicatorId));

    var requestBuilder = put(indicatorPath(indicatorId))
      .contentType(APPLICATION_JSON)
      .content("{\"order\": 1, \"label\": \"Ind 1\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("field indicator with ID [%s] was not found."
        .formatted(indicatorId)))));
  }
}
