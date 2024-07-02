package org.folio.rspec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.folio.rspec.domain.dto.FieldSubfieldChangeDto;
import org.folio.rspec.domain.dto.FieldSubfieldDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Subfield;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.rspec.service.mapper.SubfieldMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  private UUID id;
  private Field field;
  private Subfield subfield;
  private FieldSubfieldDto fieldSubfieldDto;
  private FieldSubfieldChangeDto changeDto;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();

    field = new Field();
    field.setId(id);

    subfield = new Subfield();
    subfield.setField(field);

    fieldSubfieldDto = new FieldSubfieldDto();
    changeDto = new FieldSubfieldChangeDto();
  }

  @Test
  void testFindFieldSubfields() {
    var subfields = new ArrayList<>(Collections.singletonList(subfield));

    when(subfieldRepository.findByFieldId(any(UUID.class))).thenReturn(subfields);
    when(subfieldMapper.toDto(any(Subfield.class))).thenReturn(fieldSubfieldDto);

    var result = subfieldService.findFieldSubfields(id);

    verify(subfieldRepository, times(1)).findByFieldId(any(UUID.class));

    assertEquals(1, result.getTotalRecords());
  }

  @Test
  void testCreateLocalSubfield() {
    when(subfieldMapper.toEntity(any(FieldSubfieldChangeDto.class))).thenReturn(subfield);
    when(subfieldRepository.save(any(Subfield.class))).thenReturn(subfield);
    when(subfieldMapper.toDto(any(Subfield.class))).thenReturn(fieldSubfieldDto);

    var result = subfieldService.createLocalSubfield(field, changeDto);

    verify(subfieldRepository, times(1)).save(any(Subfield.class));

    assertEquals(fieldSubfieldDto, result);
  }
}
