package org.folio.rspec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.repository.FieldRepository;
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
}
