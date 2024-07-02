package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.FieldIndicatorDtoCollection;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.FieldRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ResourceNotFoundException.Resource;
import org.folio.rspec.service.mapper.SpecificationFieldMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  private SpecificationFieldMapper specificationFieldMapper;
  @Mock
  private FieldIndicatorService indicatorService;

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
    final var createDto = new SpecificationFieldChangeDto();
    final var fieldEntity = new Field();

    ArgumentCaptor<Field> fieldCaptor = captor();

    when(specificationFieldMapper.toEntity(createDto)).thenReturn(fieldEntity);
    when(fieldRepository.save(fieldCaptor.capture())).thenReturn(fieldEntity);
    when(specificationFieldMapper.toDto(fieldEntity)).thenReturn(new SpecificationFieldDto());

    service.createLocalField(specification, createDto);

    verify(specificationFieldMapper).toEntity(createDto);
    verify(fieldRepository).save(fieldEntity);

    assertEquals(Scope.LOCAL, fieldCaptor.getValue().getScope());
    assertEquals(specification, fieldCaptor.getValue().getSpecification());
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
    assertThat(actual.getIndicators().get(0)).isEqualTo(expected.getIndicators().get(0));
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
    var field = new Field();
    field.setId(fieldId);
    var createDto = new FieldIndicatorChangeDto();
    var expected = new FieldIndicatorDto().fieldId(fieldId);

    when(fieldRepository.findById(fieldId))
      .thenReturn(Optional.of(field));
    when(indicatorService.createLocalIndicator(field, createDto))
      .thenReturn(expected);

    var actual = service.createLocalIndicator(fieldId, createDto);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCreateLocalIndicator_absentField() {
    var fieldId = UUID.randomUUID();
    var createDto = new FieldIndicatorChangeDto();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.createLocalIndicator(fieldId, createDto));

    verifyNoInteractions(indicatorService);

    assertThat(actual.getId()).isEqualTo(fieldId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_DEFINITION);
  }
}
