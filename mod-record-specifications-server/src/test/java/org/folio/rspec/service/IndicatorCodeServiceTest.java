package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.service.mapper.IndicatorCodeMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class IndicatorCodeServiceTest {

  @InjectMocks
  private IndicatorCodeService service;

  @Mock
  private IndicatorCodeRepository repository;
  @Mock
  private IndicatorCodeMapper mapper;

  @Test
  void testFindIndicatorCodes() {
    var indicatorId = UUID.randomUUID();
    var code = new IndicatorCode();
    var codeDto = new IndicatorCodeDto();

    when(repository.findByIndicatorId(indicatorId))
      .thenReturn(List.of(code));
    when(mapper.toDto(code))
      .thenReturn(codeDto);

    var actual = service.findIndicatorCodes(indicatorId);

    assertThat(actual.getTotalRecords()).isEqualTo(1);
    assertThat(actual.getCodes()).hasSize(1);
    assertThat(actual.getCodes().get(0)).isEqualTo(codeDto);
  }

  @Test
  void testFindIndicatorCodes_empty() {
    var indicatorId = UUID.randomUUID();

    var result = service.findIndicatorCodes(indicatorId);

    assertThat(result.getTotalRecords()).isZero();
    assertThat(result.getCodes()).isEmpty();
  }

  @Test
  void testCreateLocalCode() {
    var indicatorId = UUID.randomUUID();
    var indicator = new Indicator();
    indicator.setId(indicatorId);
    var createDto = new IndicatorCodeChangeDto();
    var code = new IndicatorCode();
    var expected = new IndicatorCodeDto();

    var codeCaptor = ArgumentCaptor.<IndicatorCode>captor();

    when(mapper.toEntity(createDto)).thenReturn(code);
    when(repository.save(codeCaptor.capture())).thenReturn(code);
    when(mapper.toDto(code)).thenReturn(expected);

    var actual = service.createLocalCode(indicator, createDto);

    var captured = codeCaptor.getValue();
    assertThat(actual).isEqualTo(expected);
    assertThat(captured.getScope()).isEqualTo(Scope.LOCAL);
    assertThat(captured.getIndicator()).isEqualTo(indicator);
  }
}
