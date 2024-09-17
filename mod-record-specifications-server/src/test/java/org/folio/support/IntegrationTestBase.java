package org.folio.support;

import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.folio.support.ApiEndpoints.specificationFieldsPath;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.Callable;
import lombok.SneakyThrows;
import org.folio.rspec.RecordSpecificationsApp;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.tenant.domain.dto.Parameter;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.jeasy.random.EasyRandom;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@EnablePostgres
@SpringBootTest(classes = RecordSpecificationsApp.class)
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@Import(IntegrationTestBase.IntegrationTestConfiguration.class)
public class IntegrationTestBase {

  protected static MockMvc mockMvc;
  protected static ObjectMapper objectMapper = new ObjectMapper();
  protected static EasyRandom easyRandom = new EasyRandom();

  @Autowired
  protected FolioModuleMetadata moduleMetadata;

  @BeforeAll
  protected static void setUpBeans(@Autowired MockMvc mockMvc) {
    IntegrationTestBase.mockMvc = mockMvc;
  }

  protected static void setUpTenant() {
    setUpTenant(false, false);
  }

  protected static void setUpTenant(boolean loadReference, boolean syncSpecifications) {
    setUpTenant(TENANT_ID, loadReference, syncSpecifications);
  }

  protected static void setUpTenant(String tenantId, boolean loadReference, boolean syncSpecifications) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.add(XOkapiHeaders.TENANT, tenantId);
    httpHeaders.add(XOkapiHeaders.USER_ID, USER_ID);

    var tenantAttributes = new TenantAttributes().moduleTo("mod-record-specifications")
      .addParametersItem(new Parameter("loadReference").value(String.valueOf(loadReference)))
      .addParametersItem(new Parameter("syncSpecifications").value(String.valueOf(syncSpecifications)));
    doPost("/_/tenant", tenantAttributes, httpHeaders);
  }

  @SneakyThrows
  protected static void removeTenant() {
    removeTenant(TENANT_ID);
  }

  @SneakyThrows
  protected static void removeTenant(String tenantId) {
    var httpHeaders = defaultHeaders();
    httpHeaders.set(XOkapiHeaders.TENANT, tenantId);
    var tenantAttributes = new TenantAttributes().moduleFrom("mod-record-specifications").purge(true);
    doPost("/_/tenant", tenantAttributes, httpHeaders, UUID.randomUUID().toString())
      .andExpect(status().isNoContent());
  }

  protected static HttpHeaders defaultHeaders() {
    var httpHeaders = new HttpHeaders();

    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
    httpHeaders.add(XOkapiHeaders.USER_ID, USER_ID);

    return httpHeaders;
  }

  @SneakyThrows
  protected static ResultActions tryDelete(String uri, Object... args) {
    return tryDelete(uri, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions tryDelete(String uri, HttpHeaders headers, Object... args) {
    return tryDoHttpMethod(delete(uri, args), null, headers);
  }

  @SneakyThrows
  protected static ResultActions doDelete(String uri, HttpHeaders headers, Object... args) {
    return tryDelete(uri, headers, args).andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static ResultActions doDelete(String uri, Object... args) {
    return doDelete(uri, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions tryGet(String uri, Object... args) {
    return tryGet(uri, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions tryGet(String uri, HttpHeaders headers, Object... args) {
    return tryDoHttpMethod(get(uri, args), null, headers);
  }

  @SneakyThrows
  protected static ResultActions doGet(String uri, Object... args) {
    return doGet(uri, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions doGet(String uri, HttpHeaders headers, Object... args) {
    return tryGet(uri, headers, args).andExpect(status().isOk());
  }

  @SneakyThrows
  protected static ResultActions tryPut(String uri, Object body, HttpHeaders headers, Object... args) {
    return tryDoHttpMethod(put(uri, args), body, headers);
  }

  @SneakyThrows
  protected static ResultActions tryPut(String uri, Object body, Object... args) {
    return tryPut(uri, body, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions doPut(String uri, Object body, HttpHeaders headers, Object... args) {
    return tryPut(uri, body, headers, args).andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static ResultActions doPut(String uri, Object body, Object... args) {
    return doPut(uri, body, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions tryPatch(String uri, Object body, Object... args) {
    return tryDoHttpMethod(patch(uri, args), body);
  }

  @SneakyThrows
  protected static ResultActions doPatch(String uri, Object body, Object... args) {
    return tryPatch(uri, body, args).andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static ResultActions tryPost(String uri, Object body, Object... args) {
    return tryPost(uri, body, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions tryPost(String uri, Object body, HttpHeaders headers, Object... args) {
    return tryDoHttpMethod(post(uri, args), body, headers);
  }

  @SneakyThrows
  protected static ResultActions doPost(String uri, Object body, Object... args) {
    return doPost(uri, body, defaultHeaders(), args);
  }

  @SneakyThrows
  protected static ResultActions doPost(String uri, Object body, HttpHeaders headers, Object... args) {
    return tryPost(uri, body, headers, args).andExpect(status().is2xxSuccessful());
  }

  @SneakyThrows
  protected static <T> T doPostAndReturn(String uri, Object body, Class<T> responseClass, Object... args) {
    var result = doPost(uri, body, defaultHeaders(), args).andReturn();
    return contentAsObj(result, responseClass);
  }

  @SneakyThrows
  protected static String asJson(Object value) {
    return objectMapper.writeValueAsString(value);
  }

  @SneakyThrows
  protected static <T> T contentAsObj(MvcResult result, Class<T> objectClass) {
    var contentAsBytes = result.getResponse().getContentAsByteArray();
    return objectMapper.readValue(contentAsBytes, objectClass);
  }

  protected <T> ResultMatcher exceptionMatch(Class<T> type) {
    return result -> MatcherAssert.assertThat(result.getResolvedException(), instanceOf(type));
  }

  protected ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.errors.[*].message", hasItem(errorMessageMatcher));
  }

  protected ResultMatcher errorTypeMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.errors.[*].type", hasItem(errorMessageMatcher));
  }

  protected ResultMatcher errorParameterMatch(String parameterName) {
    return jsonPath("$.errors.[*].parameters.[*].key", hasItem(parameterName));
  }

  protected SpecificationFieldChangeDto localTestField(String tag) {
    return new SpecificationFieldChangeDto()
      .tag(tag)
      .label(easyRandom.nextObject(String.class))
      .deprecated(easyRandom.nextBoolean())
      .repeatable(easyRandom.nextBoolean())
      .required(easyRandom.nextBoolean())
      .url("http://www." + easyRandom.nextObject(String.class) + ".com");
  }

  protected FieldIndicatorChangeDto localTestIndicator(Integer order) {
    return new FieldIndicatorChangeDto()
      .order(order)
      .label("Ind " + order);
  }

  protected SubfieldChangeDto localTestSubfield(String code, String label) {
    return new SubfieldChangeDto().label(label).code(code);
  }

  protected IndicatorCodeChangeDto localTestCode(String code) {
    return new IndicatorCodeChangeDto()
      .code(code)
      .deprecated(true)
      .label(easyRandom.nextObject(String.class));
  }

  protected String createLocalField(SpecificationFieldChangeDto localTestField) throws UnsupportedEncodingException {
    return JsonPath.read(
      doPost(specificationFieldsPath(TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID), localTestField)
        .andReturn()
        .getResponse().getContentAsString(),
      "$.id").toString();
  }

  protected String createLocalField(String tag) throws UnsupportedEncodingException {
    var dto = localTestField(tag);
    return createLocalField(dto);
  }

  protected String createLocalIndicator(String fieldId, FieldIndicatorChangeDto localTestIndicator) {
    return doPostAndReturn(fieldIndicatorsPath(fieldId), localTestIndicator, FieldIndicatorDto.class)
      .getId().toString();
  }

  protected String createLocalIndicator(String fieldId) {
    var dto = localTestIndicator(1);
    return createLocalIndicator(fieldId, dto);
  }

  protected String createLocalCode(String indicatorId, IndicatorCodeChangeDto localTestCode) {
    return doPostAndReturn(indicatorCodesPath(indicatorId), localTestCode, IndicatorCodeDto.class)
      .getId().toString();
  }

  protected String createLocalCode(String indicatorId) {
    var dto = localTestCode("a");
    return createLocalCode(indicatorId, dto);
  }

  @SneakyThrows
  protected <T> T executeInContext(Callable<T> callable) {
    return executeInContext(TENANT_ID, callable);
  }

  @SneakyThrows
  protected <T> T executeInContext(String tenantId, Callable<T> callable) {
    try (var fex = new FolioExecutionContextSetter(new FolioExecutionContext() {
      @Override
      public String getTenantId() {
        return tenantId;
      }

      @Override
      public FolioModuleMetadata getFolioModuleMetadata() {
        return moduleMetadata;
      }
    })) {
      return callable.call();
    }
  }

  @AfterAll
  static void cleanUp() {
    removeTenant();
  }

  @NotNull
  private static ResultActions tryDoHttpMethod(MockHttpServletRequestBuilder builder, Object body,
                                               HttpHeaders headers) throws Exception {
    String content;
    if (body == null) {
      content = "";
    } else if (body instanceof String stringBody) {
      content = stringBody;
    } else {
      content = asJson(body);
    }
    return mockMvc.perform(builder
        .content(content)
        .headers(headers))
      .andDo(log());
  }

  @NotNull
  private static ResultActions tryDoHttpMethod(MockHttpServletRequestBuilder builder, Object body) throws Exception {
    return tryDoHttpMethod(builder, body, defaultHeaders());
  }

  @TestConfiguration
  public static class IntegrationTestConfiguration {

    @Bean
    public DatabaseHelper databaseHelper(JdbcTemplate jdbcTemplate, FolioModuleMetadata moduleMetadata) {
      return new DatabaseHelper(moduleMetadata, jdbcTemplate);
    }
  }

}
