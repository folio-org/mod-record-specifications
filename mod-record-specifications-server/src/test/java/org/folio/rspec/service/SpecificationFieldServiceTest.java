package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.builders.FieldBuilder.local;
import static org.folio.support.builders.FieldBuilder.standard;
import static org.folio.support.builders.FieldBuilder.system;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
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
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.Subfield;
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

  private static Stream<Arguments> updateFieldTestData() {
    return Stream.of(
      arguments(system().buildEntity(), system().buildChangeDto()),
      arguments(standard().buildEntity(), standard().buildChangeDto()),
      arguments(local().buildEntity(), local().buildChangeDto())
    );
  }
}
