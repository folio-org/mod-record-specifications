package org.folio.rspec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.integration.kafka.EventProducer;
import org.folio.rspec.service.mapper.SubfieldMapper;
import org.folio.rspec.service.validation.scope.subfield.SubfieldSystemScopeValidator;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SubfieldServiceTest {

  @InjectMocks
  private SubfieldService subfieldService;

  @Mock
  private SubfieldRepository subfieldRepository;

  @Mock
  private SubfieldMapper subfieldMapper;

  @Mock
  private EventProducer<UUID, SpecificationUpdatedEvent> eventProducer;

  private UUID subfieldId;
  private UUID specificationId;
  private Field field;
  private Subfield subfield;
  private SubfieldDto fieldSubfieldDto;
  private SubfieldChangeDto changeDto;

  @BeforeEach
  void setUp() {
    subfieldId = UUID.randomUUID();
    specificationId = UUID.randomUUID();

    var specification = new Specification();
    specification.setId(specificationId);

    field = new Field();
    field.setSpecification(specification);

    subfield = new Subfield();
    subfield.setField(field);

    fieldSubfieldDto = new SubfieldDto();
    changeDto = new SubfieldChangeDto();
  }

  @Test
  void testFindFieldSubfields() {
    var subfields = new ArrayList<>(Collections.singletonList(subfield));

    when(subfieldRepository.findByFieldId(any(UUID.class))).thenReturn(subfields);
    when(subfieldMapper.toDto(any(Subfield.class))).thenReturn(fieldSubfieldDto);

    var result = subfieldService.findFieldSubfields(subfieldId);

    verify(subfieldRepository, times(1)).findByFieldId(any(UUID.class));

    assertEquals(1, result.getTotalRecords());
  }

  @Test
  void testCreateLocalSubfield() {
    when(subfieldMapper.toEntity(any(SubfieldChangeDto.class))).thenReturn(subfield);
    when(subfieldRepository.save(any(Subfield.class))).thenReturn(subfield);
    when(subfieldMapper.toDto(any(Subfield.class))).thenReturn(fieldSubfieldDto);

    var result = subfieldService.createLocalSubfield(field, changeDto);

    verify(subfieldRepository).save(any(Subfield.class));

    assertEquals(fieldSubfieldDto, result);
  }

  @Test
  void testUpdateSubfield_positive() {
    doNothing().when(subfieldMapper).update(subfield, changeDto);
    when(subfieldMapper.toDto(any(Subfield.class))).thenReturn(fieldSubfieldDto);
    when(subfieldRepository.save(any(Subfield.class))).thenReturn(subfield);
    when(subfieldRepository.findById(subfieldId)).thenReturn(Optional.of(subfield));
    subfield.setScope(Scope.LOCAL);

    var result = subfieldService.updateSubfield(subfieldId, changeDto);

    verify(subfieldRepository).save(any(Subfield.class));
    verify(eventProducer).sendEvent(specificationId);

    assertEquals(fieldSubfieldDto, result);
  }

  @Test
  void testUpdateSubfield_negative_unsupportedScopeChange() {
    when(subfieldRepository.findById(subfieldId)).thenReturn(Optional.of(subfield));
    subfield.setScope(Scope.SYSTEM);
    subfield.setCode("a");
    changeDto.setCode("b");
    subfieldService.setFieldValidators(List.of(new SubfieldSystemScopeValidator()));

    assertThrows(ScopeModificationNotAllowedException.class,
      () -> subfieldService.updateSubfield(subfieldId, changeDto));

    verifyNoMoreInteractions(subfieldRepository);
    verifyNoInteractions(eventProducer);
  }

  @Test
  void testUpdateSubfield_negative_subfieldNotExist() {
    when(subfieldRepository.findById(subfieldId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
      () -> subfieldService.updateSubfield(subfieldId, changeDto));

    verifyNoMoreInteractions(subfieldRepository);
    verifyNoInteractions(eventProducer);
  }

  @Test
  void testDeleteSubfield_positive() {
    when(subfieldRepository.findById(subfieldId)).thenReturn(Optional.of(subfield));
    doNothing().when(subfieldRepository).delete(any(Subfield.class));
    subfield.setScope(Scope.LOCAL);

    subfieldService.deleteSubfield(subfieldId);

    verify(subfieldRepository).delete(any(Subfield.class));
    verify(eventProducer).sendEvent(specificationId);
  }

  @ParameterizedTest
  @EnumSource(value = Scope.class, names = "LOCAL", mode = EnumSource.Mode.EXCLUDE)
  void testDeleteSubfield_negative_unsupportedScopeDeletion(Scope scope) {
    when(subfieldRepository.findById(subfieldId)).thenReturn(Optional.of(subfield));
    subfield.setScope(scope);

    assertThrows(ScopeModificationNotAllowedException.class,
      () -> subfieldService.deleteSubfield(subfieldId));

    verifyNoMoreInteractions(subfieldRepository);
    verifyNoInteractions(eventProducer);
  }

  @Test
  void testDeleteSubfield_negative_subfieldNotExist() {
    when(subfieldRepository.findById(subfieldId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
      () -> subfieldService.deleteSubfield(subfieldId));

    verifyNoMoreInteractions(subfieldRepository);
    verifyNoInteractions(eventProducer);
  }
}
