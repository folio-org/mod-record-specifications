package org.folio.api;

import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.rspec.domain.entity.Indicator.INDICATOR_TABLE_NAME;
import static org.folio.rspec.domain.entity.IndicatorCode.INDICATOR_CODE_TABLE_NAME;
import static org.folio.support.ApiEndpoints.codePath;
import static org.folio.support.ApiEndpoints.fieldIndicatorsPath;
import static org.folio.support.ApiEndpoints.indicatorCodesPath;
import static org.folio.support.ApiEndpoints.indicatorPath;
import static org.folio.support.KafkaUtils.createAndStartTestConsumer;
import static org.folio.support.TestConstants.BIBLIOGRAPHIC_SPECIFICATION_ID;
import static org.folio.support.TestConstants.TENANT_ID;
import static org.folio.support.TestConstants.USER_ID;
import static org.folio.support.TestConstants.specificationUpdatedTopic;
import static org.folio.support.builders.FieldBuilder.local;
import static org.folio.support.builders.FieldBuilder.standard;
import static org.folio.support.builders.IndicatorBuilder.basic;
import static org.folio.support.builders.IndicatorCodeBuilder.localCode;
import static org.folio.support.builders.IndicatorCodeBuilder.standardCode;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import org.folio.rspec.domain.dto.ErrorCode;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.spring.testing.extension.DatabaseCleanup;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.support.IntegrationTestBase;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.dao.DataIntegrityViolationException;

@IntegrationTest
@DatabaseCleanup(tables = {INDICATOR_CODE_TABLE_NAME, INDICATOR_TABLE_NAME, FIELD_TABLE_NAME}, tenants = TENANT_ID)
class SpecificationStorageIndicatorsApiIT extends IntegrationTestBase {

  @Autowired
  private FieldRepository fieldRepository;
  @Autowired
  private IndicatorRepository indicatorRepository;
  @Autowired
  private IndicatorCodeRepository indicatorCodeRepository;

  @BeforeAll
  static void beforeAll() {
    setUpTenant();
  }

  @BeforeEach
  void setUp(@Autowired KafkaProperties kafkaProperties) {
    consumerRecords = new LinkedBlockingQueue<>();
    container =
      createAndStartTestConsumer(specificationUpdatedTopic(),
        consumerRecords, kafkaProperties, SpecificationUpdatedEvent.class);
  }

  @AfterEach
  void tearDown() {
    consumerRecords.clear();
    container.stop();
  }

  @Test
  void getIndicatorCodes_shouldReturn200AndAllCodesForIndicator() throws Exception {
    var fieldId = createLocalField("101");
    var indId = createLocalIndicator(fieldId);
    var code1 = localTestCode("#");
    var code2 = localTestCode("1");
    code2.deprecated(false);
    doPost(indicatorCodesPath(indId), code1);
    doPost(indicatorCodesPath(indId), code2);

    doGet(indicatorCodesPath(indId))
      .andExpect(jsonPath("totalRecords", is(2)))
      .andExpect(jsonPath("codes.size()", is(2)))
      .andExpect(jsonPath("codes.[*].id", everyItem(notNullValue())))
      .andExpect(jsonPath("codes.[*].indicatorId", everyItem(is(indId))))
      .andExpect(jsonPath("codes.[*].code", hasItems(code1.getCode(), code2.getCode())))
      .andExpect(jsonPath("codes.[*].label", hasItems(code1.getLabel(), code2.getLabel())))
      .andExpect(jsonPath("codes.[*].deprecated", hasItems(code1.getDeprecated(), code2.getDeprecated())))
      .andExpect(jsonPath("codes.[*].scope", everyItem(is(Scope.LOCAL.getValue()))))
      .andExpect(jsonPath("codes.[*].metadata.createdDate", everyItem(notNullValue())))
      .andExpect(jsonPath("codes.[*].metadata.createdByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("codes.[*].metadata.updatedByUserId", everyItem(is(USER_ID))))
      .andExpect(jsonPath("codes.[*].metadata.updatedDate", everyItem(notNullValue())));
  }

  @Test
  void createIndicatorLocalCode_shouldReturn201AndCreatedCode() throws Exception {
    var fieldId = createLocalField("102");
    var indId = createLocalIndicator(fieldId);
    var code = localTestCode("1");

    var result = doPost(indicatorCodesPath(indId), code)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()))
      .andExpect(jsonPath("$.indicatorId", is(indId)))
      .andExpect(jsonPath("$.code", is(code.getCode())))
      .andExpect(jsonPath("$.label", is(code.getLabel())))
      .andExpect(jsonPath("$.deprecated", is(code.getDeprecated())))
      .andExpect(jsonPath("$.scope", is(Scope.LOCAL.getValue())))
      .andExpect(jsonPath("$.metadata.createdDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.createdByUserId", is(USER_ID)))
      .andExpect(jsonPath("$.metadata.updatedDate", notNullValue()))
      .andExpect(jsonPath("$.metadata.updatedByUserId", is(USER_ID)))
      .andReturn();

    var createdCodeId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

    doGet(indicatorCodesPath(indId))
      .andExpect(jsonPath("$.codes.[*].id", hasItem(createdCodeId)));

    assertSpecificationUpdatedEvents(3);
  }

  @Test
  void createIndicatorLocalCode_shouldReturn400WhenIndicatorCodeAlreadyExist() throws Exception {
    var fieldId = createLocalField("102");
    var indId = createLocalIndicator(fieldId);
    var code = localTestCode("1");

    doPost(indicatorCodesPath(indId), code);

    tryPost(indicatorCodesPath(indId), code)
      .andExpect(status().isBadRequest())
      .andExpect(exceptionMatch(DataIntegrityViolationException.class))
      .andExpect(errorTypeMatch(is(ErrorCode.DUPLICATE_INDICATOR_CODE.getType())))
      .andExpect(errorMessageMatch(is("The 'code' must be unique.")));
  }

  @Test
  void updateIndicator_shouldReturn201AndUpdateLocalIndicator() throws Exception {
    var localTestField = local().buildChangeDto();
    var createdFieldId = createLocalField(localTestField);
    var localTestIndicator = localTestIndicator(1);
    var createdIndicatorId = createLocalIndicator(createdFieldId, localTestIndicator);

    localTestIndicator.setOrder(2);
    localTestIndicator.setLabel("Ind 2");

    doPut(indicatorPath(createdIndicatorId), localTestIndicator);

    doGet(fieldIndicatorsPath(createdFieldId))
      .andExpect(jsonPath("$.indicators.[*]", hasItem(Matchers.<Map<String, Object>>allOf(
        hasEntry("id", createdIndicatorId),
        hasEntry("order", 2),
        hasEntry("label", "Ind 2")
      ))));

    assertSpecificationUpdatedEvents(3);
  }

  @Test
  void updateIndicator_shouldReturn400WhenIndicatorIsNotAllowedToUpdate() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(standard().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();
    var createdIndicatorId = Objects.requireNonNull(executeInContext(
      () -> indicatorRepository.save(basic().fieldId(createdFieldId).buildEntity()))
      .getId());

    tryPut(indicatorPath(createdIndicatorId), basic().label("changed").buildChangeDto())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'indicator' modification is not allowed for standard scope.")));
  }

  @Test
  void updateIndicator_shouldReturn400WhenIndicatorForOrderAlreadyExist() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(local().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();
    var createdIndicator2Id = Objects.requireNonNull(executeInContext(
      () -> {
        var entity1 = basic().fieldId(createdFieldId).buildEntity();
        var entity2 = basic().fieldId(createdFieldId)
          .order(2)
          .label("Ind 2")
          .buildEntity();
        indicatorRepository.saveAll(List.of(entity1, entity2));
        return entity2.getId();
      }));

    tryPut(indicatorPath(createdIndicator2Id), basic().order(1).buildChangeDto())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'order' must be unique.")));
  }

  @Test
  void updateIndicator_shouldReturn404WhenIndicatorNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPut(indicatorPath(notExistId), basic().buildChangeDto())
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("field indicator with ID [%s] was not found.".formatted(notExistId))));
  }

  @Test
  void deleteIndicatorCode_shouldReturn204AndDeleteLocalIndicatorCode() throws Exception {
    var fieldId = createLocalField("103");
    var indId = createLocalIndicator(fieldId);
    var codeId = createLocalCode(indId);

    doDelete(codePath(codeId));

    doGet(indicatorCodesPath(indId))
      .andExpect(jsonPath("$.codes.[*].id", not(hasItem(codeId))));

    assertSpecificationUpdatedEvents(4);
  }

  @Test
  void deleteIndicatorCode_shouldReturn404WhenIndicatorCodeNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryDelete(codePath(notExistId))
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("indicator code with ID [%s] was not found.".formatted(notExistId))));
  }

  @Test
  void deleteIndicatorCode_shouldReturn400WhenIndicatorCodeIsNotAllowedToDelete() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(standard().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();
    var createdIndicatorId = Objects.requireNonNull(executeInContext(
      () -> indicatorRepository.save(basic().fieldId(createdFieldId).buildEntity()))
      .getId());
    var createdCodeId = Objects.requireNonNull(executeInContext(
      () -> indicatorCodeRepository.save(standardCode().indicatorId(createdIndicatorId).buildEntity()))
      .getId());

    tryDelete(codePath(createdCodeId))
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("A standard scope indicator_code cannot be deleted.")));
  }

  @Test
  void updateIndicatorCode_shouldReturn201AndUpdateLocalIndicatorCode() throws Exception {
    var createdFieldId = createLocalField("250");
    var createdIndicatorId = createLocalIndicator(createdFieldId);
    var localTestCode = localTestCode("a");
    var createdCodeId = createLocalCode(createdIndicatorId, localTestCode);

    var updatedCode = localTestCode("b").deprecated(false);

    doPut(codePath(createdCodeId), updatedCode);

    doGet(indicatorCodesPath(createdIndicatorId))
      .andExpect(jsonPath("$.codes.[*]", hasItem(Matchers.<Map<String, Object>>allOf(
        hasEntry("id", createdCodeId),
        hasEntry("code", updatedCode.getCode()),
        hasEntry("label", updatedCode.getLabel()),
        hasEntry("deprecated", updatedCode.getDeprecated())
      ))));

    assertSpecificationUpdatedEvents(4);
  }

  @Test
  void updateIndicatorCode_shouldReturn400WhenIndicatorCodeIsNotAllowedToUpdate() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(standard().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();
    var createdIndicatorId = Objects.requireNonNull(executeInContext(
      () -> indicatorRepository.save(basic().fieldId(createdFieldId).buildEntity()))
      .getId());
    var createdCodeId = Objects.requireNonNull(executeInContext(
      () -> indicatorCodeRepository.save(standardCode().indicatorId(createdIndicatorId).buildEntity()))
      .getId());

    tryPut(codePath(createdCodeId), localCode().label("changed").buildChangeDto())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'indicator_code' modification is not allowed for standard scope.")));
  }

  @Test
  void updateIndicatorCode_shouldReturn400WhenCodeForIndicatorAlreadyExist() throws Exception {
    var createdFieldId = executeInContext(
      () -> fieldRepository.save(local().specificationId(BIBLIOGRAPHIC_SPECIFICATION_ID).buildEntity()))
      .getId();
    var createdIndicatorId = Objects.requireNonNull(executeInContext(
      () -> indicatorRepository.save(basic().fieldId(createdFieldId).buildEntity()))
      .getId());
    var createdCode2Id = Objects.requireNonNull(executeInContext(
      () -> {

        var entity1 = localCode().indicatorId(createdIndicatorId).buildEntity();
        var entity2 = localCode().indicatorId(createdIndicatorId)
          .code("b")
          .label("Label b")
          .buildEntity();
        indicatorCodeRepository.saveAll(List.of(entity1, entity2));
        return entity2.getId();
      }));

    tryPut(codePath(createdCode2Id), localCode().code("a").buildChangeDto())
      .andExpect(status().isBadRequest())
      .andExpect(errorMessageMatch(is("The 'code' must be unique.")));
  }

  @Test
  void updateIndicatorCode_shouldReturn404WhenIndicatorCodeNotExist() throws Exception {
    var notExistId = UUID.randomUUID();
    tryPut(codePath(notExistId), localCode().buildChangeDto())
      .andExpect(status().isNotFound())
      .andExpect(exceptionMatch(ResourceNotFoundException.class))
      .andExpect(errorMessageMatch(is("indicator code with ID [%s] was not found.".formatted(notExistId))));
  }

}
