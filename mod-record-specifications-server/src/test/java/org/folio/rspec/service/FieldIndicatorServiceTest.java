package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDtoCollection;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.repository.IndicatorRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.exception.ResourceNotFoundException.Resource;
import org.folio.rspec.service.mapper.FieldIndicatorMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FieldIndicatorServiceTest {

  @InjectMocks
  private FieldIndicatorService service;

  @Mock
  private IndicatorRepository repository;
  @Mock
  private FieldIndicatorMapper mapper;
  @Mock
  private IndicatorCodeService codeService;

  @Test
  void testFindFieldIndicators() {
    var fieldId = UUID.randomUUID();
    var indicator = new Indicator();
    var indicatorDto = new FieldIndicatorDto();
    when(repository.findByFieldId(fieldId)).thenReturn(List.of(indicator));
    when(mapper.toDto(indicator)).thenReturn(indicatorDto);

    var result = service.findFieldIndicators(fieldId);

    assertThat(result.getTotalRecords()).isEqualTo(1);
    assertThat(result.getIndicators()).hasSize(1);
    assertThat(result.getIndicators().get(0)).isEqualTo(indicatorDto);
  }

  @Test
  void testFindFieldIndicators_empty() {
    var fieldId = UUID.randomUUID();

    var result = service.findFieldIndicators(fieldId);

    assertThat(result.getTotalRecords()).isZero();
    assertThat(result.getIndicators()).isEmpty();
  }

  @Test
  void testCreateLocalIndicator() {
    var field = new Field();
    var createDto = new FieldIndicatorChangeDto();
    var indicator = new Indicator();
    var expected = new FieldIndicatorDto();

    var indicatorCaptor = ArgumentCaptor.<Indicator>captor();

    when(mapper.toEntity(createDto)).thenReturn(indicator);
    when(repository.save(indicatorCaptor.capture())).thenReturn(indicator);
    when(mapper.toDto(indicator)).thenReturn(expected);

    var actual = service.createLocalIndicator(field, createDto);

    assertThat(actual).isEqualTo(expected);
    assertThat(indicatorCaptor.getValue().getField()).isEqualTo(field);
  }

  @Test
  void testFindIndicatorCodes() {
    var indicatorId = UUID.randomUUID();
    var indicator = new Indicator();
    var expected = new IndicatorCodeDtoCollection().codes(List.of(new IndicatorCodeDto()));

    when(repository.findById(indicatorId))
      .thenReturn(Optional.of(indicator));
    when(codeService.findIndicatorCodes(indicatorId))
      .thenReturn(expected);

    var actual = service.findIndicatorCodes(indicatorId);

    assertThat(actual.getTotalRecords()).isNull();
    assertThat(actual.getCodes()).hasSize(1);
    assertThat(actual.getCodes().get(0)).isEqualTo(expected.getCodes().get(0));
  }

  @Test
  void testFindIndicatorCodes_absentIndicator() {
    var indicatorId = UUID.randomUUID();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.findIndicatorCodes(indicatorId));

    verifyNoInteractions(codeService);

    assertThat(actual.getId()).isEqualTo(indicatorId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_INDICATOR);
  }

  @Test
  void testCreateLocalCode() {
    var indicatorId = UUID.randomUUID();
    var indicator = new Indicator();
    var createDto = new IndicatorCodeChangeDto();
    var expected = new IndicatorCodeDto();

    when(repository.findById(indicatorId))
      .thenReturn(Optional.of(indicator));
    when(codeService.createLocalCode(indicator, createDto))
      .thenReturn(expected);

    var actual = service.createLocalCode(indicatorId, createDto);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testCreateLocalIndicator_absentField() {
    var indicatorId = UUID.randomUUID();
    var createDto = new IndicatorCodeChangeDto();

    var actual = assertThrows(ResourceNotFoundException.class, () -> service.createLocalCode(indicatorId, createDto));

    verifyNoInteractions(codeService);

    assertThat(actual.getId()).isEqualTo(indicatorId);
    assertThat(actual.getResource()).isEqualTo(Resource.FIELD_INDICATOR);
  }
}
