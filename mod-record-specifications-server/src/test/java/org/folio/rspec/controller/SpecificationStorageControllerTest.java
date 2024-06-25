package org.folio.rspec.controller;

import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.ApiEndpoints.specificationRulePath;
import static org.folio.support.ApiEndpoints.specificationRulesPath;
import static org.folio.support.ApiEndpoints.specificationSyncPath;
import static org.folio.support.ApiEndpoints.specificationsPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.folio.rspec.config.TranslationConfig;
import org.folio.rspec.config.ValidationConfig;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationFieldDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.folio.rspec.service.SpecificationService;
import org.folio.rspec.service.mapper.StringToFamilyEnumConverter;
import org.folio.rspec.service.mapper.StringToFamilyProfileEnumConverter;
import org.folio.rspec.service.mapper.StringToIncludeParamEnumConverter;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@ExtendWith(RandomParametersExtension.class)
@WebMvcTest(SpecificationStorageController.class)
@Import({ApiExceptionHandler.class, TranslationConfig.class, ValidationConfig.class})
@ComponentScan(basePackages = {"org.folio.rspec.controller.handler",
                               "org.folio.rspec.service.i18n",
                               "org.folio.spring.i18n"})
class SpecificationStorageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private GenericConversionService conversionService;

  @MockBean
  private SpecificationService specificationService;

  @BeforeEach
  public void setup() {
    conversionService.addConverter(new StringToFamilyEnumConverter());
    conversionService.addConverter(new StringToFamilyProfileEnumConverter());
    conversionService.addConverter(new StringToIncludeParamEnumConverter());
  }

  @Test
  void syncSpecification() throws Exception {
    var specificationId = UUID.randomUUID();

    var requestBuilder = post(specificationSyncPath(specificationId))
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isAccepted());

    verify(specificationService).sync(specificationId);
  }

  @Test
  void syncSpecification_failedToFetchSpecfification() throws Exception {
    var specificationId = UUID.randomUUID();

    doThrow(new SpecificationFetchingFailedException())
      .when(specificationService).sync(specificationId);

    var requestBuilder = post(specificationSyncPath(specificationId))
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("Specification fetching failed."))));
  }

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

  @CsvSource(delimiter = '|', value = {
    "family   | MARC",
    "profile  | authority and bibliographic"})
  @ParameterizedTest
  void getSpecifications_badRequest_whenInvalidEnumQueryParam(String queryParam, String possibleValues)
    throws Exception {
    var requestBuilder = get(specificationsPath())
      .queryParam(queryParam, "randomValue")
      .contentType(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("Unexpected value [randomValue]. Possible values: [%s]."
        .formatted(possibleValues)))));
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

  @Test
  void getSpecificationFields_returnSpecificationFields(@Random SpecificationFieldDto fieldDto) throws Exception {
    var specificationId = UUID.randomUUID();
    when(specificationService.findSpecificationFields(specificationId))
      .thenReturn(new SpecificationFieldDtoCollection().totalRecords(1).addFieldsItem(fieldDto));

    var requestBuilder = get(specificationFieldsPath(specificationId))
      .accept(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords", is(1)))
      .andExpect(jsonPath("$.fields.size()", is(1)))
      .andExpect(jsonPath("$.fields[0].id", is(fieldDto.getId().toString())));
  }

  @Test
  void getSpecificationFields_return404_notExistedSpecification() throws Exception {
    var specificationId = UUID.randomUUID();
    when(specificationService.findSpecificationFields(specificationId))
      .thenThrow(ResourceNotFoundException.forSpecification("id"));

    var requestBuilder = get(specificationFieldsPath(specificationId))
      .accept(APPLICATION_JSON);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(containsString("not found"))));
  }

  @Test
  void createSpecificationLocalField_createNewLocalField(@Random SpecificationFieldDto fieldDto) throws Exception {
    var specificationId = UUID.randomUUID();
    when(specificationService.createLocalField(eq(specificationId), any())).thenReturn(fieldDto);

    var requestBuilder = post(specificationFieldsPath(specificationId))
      .contentType(APPLICATION_JSON)
      .content("""
        {
          "tag": 888,
          "label": "Custom Field - Contributor Data",
          "url": "http://www.example.org/field888.html",
          "repeatable": true,
          "required": true,
          "deprecated": true
        }
        """);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", is(fieldDto.getId().toString())))
      .andExpect(jsonPath("$.tag", is(fieldDto.getTag())))
      .andExpect(jsonPath("$.label", is(fieldDto.getLabel())))
      .andExpect(jsonPath("$.url", is(fieldDto.getUrl())))
      .andExpect(jsonPath("$.repeatable", is(fieldDto.getRepeatable())))
      .andExpect(jsonPath("$.required", is(fieldDto.getRequired())))
      .andExpect(jsonPath("$.deprecated", is(fieldDto.getDeprecated())));
  }

  @Test
  void createSpecificationLocalField_return404_notExistedSpecification() throws Exception {
    var specificationId = UUID.randomUUID();
    when(specificationService.createLocalField(eq(specificationId), any()))
      .thenThrow(ResourceNotFoundException.forSpecification(specificationId));

    var requestBuilder = post(specificationFieldsPath(specificationId))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"666\", \"label\": \"Mystic field\"}");

    mockMvc.perform(requestBuilder)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("specification with ID [%s] was not found."
        .formatted(specificationId)))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"Mystic field\"}  | tag",
    "{\"tag\": \"666\"}             | label"
  })
  @ParameterizedTest
  void createSpecificationLocalField_return400_missingFieldInPayload(String content, String field) throws Exception {
    var requestBuilder = post(specificationFieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The '%s' field is required.".formatted(field)))));
  }

  @CsvSource(delimiter = '|', value = {
    "{\"label\": \"\", \"tag\": \"666\"}                             | label",
    "{\"label\": \"  \", \"tag\": \"666\"}                           | label"
  })
  @ParameterizedTest
  void createSpecificationLocalField_return400_blankRequiredFieldInPayload(String content, String field)
    throws Exception {
    var requestBuilder = post(specificationFieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content(content);

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The '%s' must be not blank.".formatted(field)))));
  }

  @ValueSource(strings = {
    "",
    "   ",
    "invalid",
    "h ttp://www.google.com",
    "http:://www.google.com",
    "www.google.com",
    "http://",
    "http://www .google.com",
    "http:/google.com",
    "htp://www.google.com",
    "http://www.goo gle.com",
    "http:/www.google.com",
    "http//www.google.com",
    "http:www.google.com",
    "http/www.google.com",
    "htt://www.google.com",
    "http:/ /www.google.com",
    "http://w ww.google.com",
    "http:/",
    "http://www.google./com",
    "http:///www.google.com"
  })
  @ParameterizedTest
  void createSpecificationLocalField_return400_invalidUrl(String invalidUrl) throws Exception {
    var requestBuilder = post(specificationFieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"666\", \"label\": \"Mystic field\", \"url\": \"%s\"}".formatted(invalidUrl));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message", hasItem(is("The 'url' field should be valid URL."))));
  }

  @Test
  void createSpecificationLocalField_return400_fieldLengthExceedLimit() throws Exception {
    String label = "a".repeat(351);
    var requestBuilder = post(specificationFieldsPath(UUID.randomUUID()))
      .contentType(APPLICATION_JSON)
      .content("{\"tag\": \"666\", \"label\": \"%s\"}".formatted(label));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.[*].message",
        hasItem(is("The 'label' field name has exceeded 350 character limit"))));
  }

}
