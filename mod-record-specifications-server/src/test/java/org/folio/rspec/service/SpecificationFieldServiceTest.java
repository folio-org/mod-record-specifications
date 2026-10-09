package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.builders.FieldBuilder.local;
import static org.folio.support.builders.FieldBuilder.standard;
import static org.folio.support.builders.FieldBuilder.system;
import static org.folio.support.builders.IndicatorCodeBuilder.localCode;
import static org.folio.support.builders.IndicatorCodeBuilder.standardCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.SubfieldDtoCollection;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.domain.entity.metadata.IndicatorMetadata;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ResourceNotFoundException.Resource;
import org.folio.rspec.exception.ResourceValidationFailedException;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.exception.ScopeModificationNotAllowedException.ModificationType;
import org.folio.rspec.integration.kafka.EventProducer;
import org.folio.rspec.service.mapper.FieldMapper;
import org.folio.rspec.service.validation.resource.FieldValidator;
import org.folio.rspec.service.validation.scope.ScopeValidator;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.builders.IndicatorBuilder;
import org.folio.support.builders.IndicatorCodeBuilder;
import org.folio.support.builders.SubfieldBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SpecificationFieldServiceTest {

  @InjectMocks
  private SpecificationFieldService service;

  @Mock
  private FieldRepository fieldRepository;
  @Mock
  private FieldMapper fieldMapper;
  @Mock
  private FieldIndicatorService indicatorService;
  @Mock
  private SubfieldService subfieldService;
  @Mock
  private FieldValidator fieldValidator;

  @Mock
  private ScopeValidator<SpecificationFieldChangeDto, Field> validator;

  @Mock
  private EventProducer<UUID, SpecificationUpdatedEvent> eventProducer;

  @BeforeEach
  void setUp() {
    when(validator.scope()).thenReturn(Scope.LOCAL, Scope.STANDARD, Scope.SYSTEM);
    service.setFieldValidators(List.of(validator, validator, validator));
  }

  @Test
  void testFindSpecificationFields() {
    var specificationId = UUID.randomUUID();
    when(fieldRepository.findBySpecificationId(specificationId))
      .thenReturn(Collections.emptyList());

    var specificationFieldDtoCollection = service.findSpecificationFields(specificationId);

    assertEquals(0, specificationFieldDtoCollection.getTotalRecords());
    assertEquals(0, specificationFieldDtoCollection.getFields().size());
  }

  @Test
  void testCreateLocalField() {
    final var specification = new Specification();
    final var createDto = local().buildChangeDto();
    final var fieldEntity = local().buildEntity();

    ArgumentCaptor<Field> fieldCaptor = captor();

    when(fieldMapper.toEntity(createDto)).thenReturn(fieldEntity);
    when(fieldRepository.save(fieldCaptor.capture())).thenReturn(fieldEntity);
    when(fieldMapper.toDto(fieldEntity)).thenReturn(new SpecificationFieldDto());

    service.createLocalField(specification, createDto);

    verify(fieldMapper).toEntity(createDto);
    verify(fieldRepository).save(fieldEntity);

    assertEquals(Scope.LOCAL, fieldCaptor.getValue().getScope());
    assertEquals(specification, fieldCaptor.getValue().getSpecification());
  }

  @Test
  void testDeleteField() {
    var field = local().buildEntity();
    var fieldId = Objects.requireNonNull(field.getId());

    when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

    service.deleteField(fieldId);

    verify(fieldRepository).delete(field);
    verify(eventProducer).sendEvent(field.getSpecification().getId());
  }

  @EnumSource(value = Scope.class, names = "LOCAL", mode = EnumSource.Mode.EXCLUDE)
  @ParameterizedTest
  void testDeleteField_throwExceptionForUnsupportedScope(Scope scope) {
    var fieldId = UUID.randomUUID();
    var field = new Field();
    field.setScope(scope);
    when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

    var exception = assertThrows(ScopeModificationNotAllowedException.class, () -> service.deleteField(fieldId));
    assertThat(exception)
      .extracting(ScopeModificationNotAllowedException::getScope,
        ScopeModificationNotAllowedException::getModificationType)
      .containsExactly(scope, ModificationType.DELETE);

    verifyNoMoreInteractions(fieldRepository);
    verifyNoInteractions(eventProducer);
  }

  @MethodSource("updateFieldTestData")
  @ParameterizedTest
  void testUpdateField(Field existed, SpecificationFieldChangeDto changeDto) {
    final var fieldId = Objects.requireNonNull(existed.getId());

    when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existed));
    doNothing().when(fieldMapper).update(existed, changeDto);
    when(fieldRepository.save(any())).thenReturn(existed);
    when(fieldMapper.toDto(existed)).thenReturn(new SpecificationFieldDto());

    service.updateField(fieldId, changeDto);

    verify(validator).validateChange(changeDto, existed);
    verify(fieldRepository).save(existed);
    verify(eventProducer).sendEvent(existed.getSpecification().getId());
  }

  @MethodSource("updateFieldTestData")
  @ParameterizedTest
  void testUpdateField_throwExceptionForUnsupportedModification(Field existed, SpecificationFieldChangeDto changeDto) {
    final var fieldId = Objects.requireNonNull(existed.getId());

    when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existed));
    doThrow(ScopeModificationNotAllowedException.forUpdate(existed.getScope(), "fieldName"))
      .when(validator).validateChange(changeDto, existed);

    var exception =
      assertThrows(ScopeModificationNotAllowedException.class, () -> service.updateField(fieldId, changeDto));
    assertThat(exception)
      .extracting(ScopeModificationNotAllowedException::getFieldName)
      .isEqualTo("fieldName");

    verifyNoMoreInteractions(fieldRepository);
    verifyNoInteractions(eventProducer);
  }

  @Test
  void testFindFieldIndicators() {
    var fieldId = UUID.randomUUID();
    var field = new Field();
    field.setId(fieldId);
    var expected = new FieldIndicatorDtoCollection().indicators(List.of(new FieldIndicatorDto().fieldId(fieldId)));

    when(fieldRepository.findById(fieldId))
      .thenReturn(Optional.of(field));
    when(indicatorService.findFieldIndicators(fieldId))
      .thenReturn(expected);

    var actual = service.findFieldIndicators(fieldId);

    assertThat(actual.getTotalRecords()).isNull();
    assertThat(actual.getIndicators()).hasSize(1);
    assertThat(actual.getIndicators().getFirst()).isEqualTo(expected.getIndicators().getFirst());
  }

  @Test
  void testFindFieldIndicators_absentField() {
    var fieldId = UUID.randomUUID();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.findFieldIndicators(fieldId));

    verifyNoInteractions(indicatorService);

    assertThat(actual.getId()).isEqualTo(fieldId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_DEFINITION);
  }

  @Test
  void testCreateLocalIndicator() {
    var fieldId = UUID.randomUUID();
    var field = local().id(fieldId).buildEntity();
    var createDto = new FieldIndicatorChangeDto();
    var expected = new FieldIndicatorDto().fieldId(fieldId);

    when(fieldRepository.findById(fieldId))
      .thenReturn(Optional.of(field));
    when(indicatorService.createLocalIndicator(field, createDto))
      .thenReturn(expected);

    var actual = service.createLocalIndicator(fieldId, createDto);

    assertThat(actual).isEqualTo(expected);

    verify(fieldValidator).validateFieldResourceCreate(field, Indicator.INDICATOR_TABLE_NAME);
    verify(eventProducer).sendEvent(field.getSpecification().getId());
  }

  @Test
  void testCreateLocalIndicator_absentField() {
    var fieldId = UUID.randomUUID();
    var createDto = new FieldIndicatorChangeDto();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.createLocalIndicator(fieldId, createDto));

    verifyNoInteractions(indicatorService);
    verifyNoInteractions(eventProducer);

    assertThat(actual.getId()).isEqualTo(fieldId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_DEFINITION);
  }

  @Test
  void testCreateLocalIndicator_validationFailed() {
    var fieldId = UUID.randomUUID();
    var field = local().id(fieldId).buildEntity();
    var createDto = new FieldIndicatorChangeDto();

    when(fieldRepository.findById(fieldId))
      .thenReturn(Optional.of(field));
    doThrow(ResourceValidationFailedException.class)
      .when(fieldValidator).validateFieldResourceCreate(field, Indicator.INDICATOR_TABLE_NAME);

    assertThrows(ResourceValidationFailedException.class, () -> service.createLocalIndicator(fieldId, createDto));

    verifyNoInteractions(indicatorService);
  }

  @Test
  void testFindFieldSubfields() {
    var fieldId = UUID.randomUUID();
    var field = new Field();
    field.setId(fieldId);
    var expected = new SubfieldDtoCollection().subfields(List.of(new SubfieldDto().fieldId(fieldId)));

    when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
    when(subfieldService.findFieldSubfields(fieldId)).thenReturn(expected);

    var actual = service.findFieldSubfields(fieldId);

    assertThat(actual.getTotalRecords()).isNull();
    assertThat(actual.getSubfields()).hasSize(1);
    assertThat(actual.getSubfields().getFirst()).isEqualTo(expected.getSubfields().getFirst());
  }

  @Test
  void testFindFieldSubfields_absentField() {
    var fieldId = UUID.randomUUID();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.findFieldSubfields(fieldId));

    verifyNoInteractions(subfieldService);

    assertThat(actual.getId()).isEqualTo(fieldId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_DEFINITION);
  }

  @Test
  void testSaveSubfield() {
    var fieldId = UUID.randomUUID();
    var field = local().id(fieldId).buildEntity();
    var subfieldDto = new SubfieldDto().fieldId(fieldId);
    var specificationId = field.getSpecification().getId();

    when(fieldRepository.findBySpecificationIdAndTag(specificationId, field.getTag()))
      .thenReturn(Optional.of(field));
    when(subfieldService.saveSubfield(field, subfieldDto)).thenReturn(subfieldDto);

    var actual = service.saveSubfield(specificationId, field.getTag(), subfieldDto);

    assertThat(actual).isEqualTo(subfieldDto);

    verify(eventProducer).sendEvent(specificationId);
  }

  @Test
  void testCreateLocalSubfield() {
    var fieldId = UUID.randomUUID();
    var field = local().id(fieldId).buildEntity();
    var createDto = new SubfieldChangeDto();
    var expected = new SubfieldDto().fieldId(fieldId);

    when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
    when(subfieldService.createLocalSubfield(field, createDto)).thenReturn(expected);

    var actual = service.createLocalSubfield(fieldId, createDto);

    assertThat(actual).isEqualTo(expected);

    verify(fieldValidator).validateFieldResourceCreate(field, Subfield.SUBFIELD_TABLE_NAME);
    verify(eventProducer).sendEvent(field.getSpecification().getId());
  }

  @Test
  void testCreateLocalSubfield_absentField() {
    var fieldId = UUID.randomUUID();
    var createDto = new SubfieldChangeDto();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.createLocalSubfield(fieldId, createDto));

    verifyNoInteractions(indicatorService);
    verifyNoInteractions(eventProducer);

    assertThat(actual.getId()).isEqualTo(fieldId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_DEFINITION);
  }

  @Test
  void testCreateLocalSubfield_validationFailed() {
    var fieldId = UUID.randomUUID();
    var field = local().id(fieldId).buildEntity();
    var createDto = new SubfieldChangeDto();

    when(fieldRepository.findById(fieldId))
      .thenReturn(Optional.of(field));
    doThrow(ResourceValidationFailedException.class)
      .when(fieldValidator).validateFieldResourceCreate(field, Subfield.SUBFIELD_TABLE_NAME);

    assertThrows(ResourceValidationFailedException.class, () -> service.createLocalSubfield(fieldId, createDto));

    verifyNoInteractions(indicatorService);
  }

  @Test
  void syncFields_preserveLocal_keepsEditableStandardFieldOverrides_whenNotContradictingSpec() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var existing = standard().tag("100").url("https://custom.example.com").required(true).deprecated(false)
      .buildEntity();
    var incoming = standard().tag("100").url("https://www.loc.gov/marc/bibliographic/bd100.html").required(false)
      .deprecated(false).buildEntity();

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(existing));

    service.syncFields(specification, List.of(incoming), true, null);

    verify(fieldRepository).deleteById(existing.getId());
    ArgumentCaptor<Field> savedField = captor();
    verify(fieldRepository).save(savedField.capture());
    assertThat(savedField.getValue().getUrl()).isEqualTo(existing.getUrl());
    assertThat(savedField.getValue().isRequired()).isEqualTo(existing.isRequired());
  }

  @Test
  void syncFields_preserveLocal_dropsCustomUrl_whenFieldBecomesDeprecated() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var existing = standard().tag("100").url("https://custom.example.com").deprecated(false).buildEntity();
    var incoming = standard().tag("100").url(null).deprecated(true).buildEntity();

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(existing));

    service.syncFields(specification, List.of(incoming), true, null);

    ArgumentCaptor<Field> savedField = captor();
    verify(fieldRepository).save(savedField.capture());
    assertThat(savedField.getValue().getUrl()).as("deprecated field must not keep a custom url").isNull();
  }

  @Test
  void syncFields_preserveLocal_doesNotPreserveRequired_forSystemScopeField() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var existing = system().tag("245").url("https://custom.example.com").required(true).deprecated(false)
      .buildEntity();
    var incoming = system().tag("245").url("https://www.loc.gov/marc/bibliographic/bd245.html").required(false)
      .deprecated(false).buildEntity();

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(existing));

    service.syncFields(specification, List.of(incoming), true, null);

    ArgumentCaptor<Field> savedField = captor();
    verify(fieldRepository).save(savedField.capture());
    assertThat(savedField.getValue().getUrl()).as("url stays editable for SYSTEM scope").isEqualTo(existing.getUrl());
    assertThat(savedField.getValue().isRequired()).as("required is locked for SYSTEM scope").isFalse();
  }

  @Test
  void syncFields_preserveLocal_keepsLocalFieldNotCoveredBySpec() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var localField = local().tag("950").buildEntity();
    var incomingField = standard().tag("100").buildEntity();

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(localField));

    service.syncFields(specification, List.of(incomingField), true, null);

    verify(fieldRepository, never()).deleteById(localField.getId());
    verify(fieldRepository, never()).deleteAllById(List.of(localField.getId()));
    ArgumentCaptor<Field> savedField = captor();
    verify(fieldRepository).save(savedField.capture());
    assertThat(savedField.getValue().getTag()).isEqualTo("100");
  }

  @Test
  void syncFields_preserveLocal_deletesStaleStandardFieldNotCoveredBySpec() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var staleField = standard().tag("900").buildEntity();
    var incomingField = standard().tag("100").buildEntity();

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(staleField));

    service.syncFields(specification, List.of(incomingField), true, null);

    verify(fieldRepository).deleteAllById(List.of(staleField.getId()));
  }

  @Test
  void syncFields_preserveLocal_keepsRequiredOverride_onStandardSubfieldOnly() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var existing = standard().tag("100").buildEntity();
    var existingStandardSubfield = SubfieldBuilder.standard().code("a").required(true).buildEntity();
    var existingSystemSubfield = SubfieldBuilder.system().code("b").required(true).buildEntity();
    existing.setSubfields(new HashSet<>(List.of(existingStandardSubfield, existingSystemSubfield)));

    var incoming = standard().tag("100").buildEntity();
    var incomingStandardSubfield = SubfieldBuilder.standard().code("a").required(false).buildEntity();
    var incomingSystemSubfield = SubfieldBuilder.system().code("b").required(false).buildEntity();
    incoming.setSubfields(new HashSet<>(List.of(incomingStandardSubfield, incomingSystemSubfield)));

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(existing));

    service.syncFields(specification, List.of(incoming), true, null);

    assertThat(incomingStandardSubfield.isRequired()).as("required stays editable for STANDARD subfields").isTrue();
    assertThat(incomingSystemSubfield.isRequired()).as("required is locked for SYSTEM subfields").isFalse();
  }

  @Test
  void syncFields_preserveLocal_keepsLocalSubfieldNotCoveredBySpec() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());

    var existing = standard().tag("100").buildEntity();
    var localSubfield = SubfieldBuilder.local().code("9").buildEntity();
    existing.setSubfields(new HashSet<>(List.of(localSubfield)));

    var incoming = standard().tag("100").buildEntity();
    incoming.setSubfields(new HashSet<>());

    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(existing));

    service.syncFields(specification, List.of(incoming), true, null);

    assertThat(incoming.getSubfields())
      .extracting(Subfield::getCode)
      .contains("9");
  }

  @Test
  void syncFields_preserveLocal_keepsIncomingValues_whenExistingFieldWithSameTagIsLocal() {
    var existing = local().tag("100").url("https://local.example.com").required(true).buildEntity();
    var incoming = standard().tag("100").url("https://www.loc.gov/100").required(false).deprecated(false)
      .buildEntity();

    reconcile(existing, incoming, null);

    assertThat(incoming.getUrl()).isEqualTo("https://www.loc.gov/100");
    assertThat(incoming.isRequired()).isFalse();
  }

  @Test
  void syncFields_preserveLocal_doesNotCarryOverNonLocalSubfieldMissingFromSpec() {
    var existing = standard().tag("100").buildEntity();
    existing.setSubfields(new HashSet<>(List.of(SubfieldBuilder.standard().code("z").buildEntity())));
    var incoming = standard().tag("100").buildEntity();

    reconcile(existing, incoming, null);

    assertThat(incoming.getSubfields()).isEmpty();
  }

  @Test
  void syncFields_preserveLocal_carriesOverIndicatorWithItsCodes_whenSpecHasNoSuchOrder() {
    var existing = standard().tag("100").buildEntity();
    existing.setIndicators(new ArrayList<>(List.of(indicator(2, localCode().code("x").label("Local x")))));
    var incoming = standard().tag("100").buildEntity();

    reconcile(existing, incoming, metadataFor("100", null));

    assertThat(incoming.getIndicators()).singleElement().satisfies(carried -> {
      assertThat(carried.getOrder()).isEqualTo(2);
      assertThat(carried.getField()).isSameAs(incoming);
      assertThat(carried.getCodes()).singleElement().satisfies(code -> {
        assertThat(code.getCode()).isEqualTo("x");
        assertThat(code.getLabel()).isEqualTo("Local x");
        assertThat(code.getScope()).isEqualTo(Scope.LOCAL);
        assertThat(code.getIndicator()).isSameAs(carried);
      });
    });
  }

  @Test
  void syncFields_preserveLocal_dropsIndicator_whenSpecMetadataKnowsItsOrder() {
    var existing = standard().tag("100").buildEntity();
    existing.setIndicators(new ArrayList<>(List.of(indicator(2, standardCode().code("x")))));
    var incoming = standard().tag("100").buildEntity();

    reconcile(existing, incoming, metadataFor("100", 2));

    assertThat(incoming.getIndicators()).isEmpty();
  }

  @Test
  void syncFields_preserveLocal_treatsIndicatorAsUnknown_whenThereIsNoSpecMetadataForIt() {
    var existing = standard().tag("100").buildEntity();
    existing.setIndicators(new ArrayList<>(List.of(indicator(1, localCode().code("x")))));
    var withoutMetadata = standard().tag("100").buildEntity();
    var otherTagOnly = standard().tag("100").buildEntity();

    reconcile(existing, withoutMetadata, null);
    reconcile(existing, otherTagOnly, metadataFor("245", 1));

    assertThat(withoutMetadata.getIndicators()).extracting(Indicator::getOrder).containsExactly(1);
    assertThat(otherTagOnly.getIndicators()).extracting(Indicator::getOrder).containsExactly(1);
  }

  @Test
  void syncFields_preserveLocal_mergesOnlyMissingLocalCodes_intoSpecIndicator() {
    var existing = standard().tag("100").buildEntity();
    existing.setIndicators(new ArrayList<>(List.of(indicator(1, localCode().code("a"),
      localCode().code("x"), standardCode().code("y")))));
    var incoming = standard().tag("100").buildEntity();
    incoming.setIndicators(new ArrayList<>(List.of(indicator(1, standardCode().code("a")))));

    reconcile(existing, incoming, null);

    assertThat(incoming.getIndicators().getFirst().getCodes())
      .extracting(IndicatorCode::getCode)
      .containsExactlyInAnyOrder("a", "x");
  }

  @Test
  void testSaveSubfield_whenFieldNotFound() {
    var specificationId = UUID.randomUUID();
    when(fieldRepository.findBySpecificationIdAndTag(specificationId, "100")).thenReturn(Optional.empty());
    var subfieldDto = new SubfieldDto();

    assertThrows(ResourceNotFoundException.class, () -> service.saveSubfield(specificationId, "100", subfieldDto));
  }

  @Test
  void syncFields_withoutPreserveLocal_wipesAndRecreatesAllFields() {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());
    var fields = List.of(standard().tag("100").buildEntity());

    service.syncFields(specification, fields, false, null);

    verify(fieldRepository).deleteBySpecificationId(specification.getId());
    verify(fieldRepository).saveAll(fields);
    verify(fieldRepository, never()).findBySpecificationId(specification.getId());
  }

  @Test
  void syncFields_preserveLocal_leavesValuesAlone_whenNothingDiffersOnStandardRows() {
    var existing = standard().tag("100").url("https://www.loc.gov/100").required(true).buildEntity();
    existing.setSubfields(new HashSet<>(List.of(SubfieldBuilder.standard().code("a").required(true).buildEntity())));
    var incoming = standard().tag("100").url("https://www.loc.gov/100").required(true).deprecated(false)
      .buildEntity();
    var incomingSubfield = SubfieldBuilder.standard().code("a").required(true).buildEntity();
    incoming.setSubfields(new HashSet<>(List.of(incomingSubfield)));

    reconcile(existing, incoming, null);

    assertThat(incoming.getUrl()).isEqualTo("https://www.loc.gov/100");
    assertThat(incoming.isRequired()).isTrue();
    assertThat(incomingSubfield.isRequired()).isTrue();
  }

  @Test
  void syncFields_preserveLocal_treatsIndicatorAsUnknown_whenSpecMetadataHasNoIndicatorsForTheTag() {
    var existing = standard().tag("100").buildEntity();
    existing.setIndicators(new ArrayList<>(List.of(indicator(1, localCode().code("x")))));
    var incoming = standard().tag("100").buildEntity();
    var metadata = new SpecificationMetadata();
    metadata.setFields(new HashMap<>(Map.of("100",
      new FieldMetadata("id", "100", Scope.STANDARD.name(), false, null, null, null, null, null, null, null))));

    reconcile(existing, incoming, metadata);

    assertThat(incoming.getIndicators()).extracting(Indicator::getOrder).containsExactly(1);
  }

  private void reconcile(Field existing, Field incoming, SpecificationMetadata metadata) {
    var specification = new Specification();
    specification.setId(UUID.randomUUID());
    when(fieldRepository.findBySpecificationId(specification.getId())).thenReturn(List.of(existing));

    service.syncFields(specification, List.of(incoming), true, metadata);
  }

  private Indicator indicator(int order, IndicatorCodeBuilder... codes) {
    var indicator = IndicatorBuilder.basic().order(order).buildEntity();
    indicator.setCodes(new ArrayList<>(Arrays.stream(codes).map(IndicatorCodeBuilder::buildEntity).toList()));
    return indicator;
  }

  // metadata knowing the tag and, if given, an indicator of that order under it
  private SpecificationMetadata metadataFor(String tag, Integer knownIndicatorOrder) {
    var fieldMetadata = new FieldMetadata(tag, Scope.STANDARD.name());
    if (knownIndicatorOrder != null) {
      fieldMetadata.indicators().put(String.valueOf(knownIndicatorOrder),
        new IndicatorMetadata(String.valueOf(knownIndicatorOrder)));
    }
    var metadata = new SpecificationMetadata();
    metadata.setFields(new HashMap<>(Map.of(tag, fieldMetadata)));
    return metadata;
  }

  private static Stream<Arguments> updateFieldTestData() {
    return Stream.of(
      arguments(system().buildEntity(), system().buildChangeDto()),
      arguments(standard().buildEntity(), standard().buildChangeDto()),
      arguments(local().buildEntity(), local().buildChangeDto())
    );
  }
}
