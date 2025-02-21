package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.builders.IndicatorCodeBuilder.localCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;
import org.folio.rspec.domain.repository.IndicatorCodeRepository;
import org.folio.rspec.exception.ScopeModificationNotAllowedException;
import org.folio.rspec.integration.kafka.EventProducer;
import org.folio.rspec.service.mapper.IndicatorCodeMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
  @Mock
  private EventProducer<UUID, SpecificationUpdatedEvent> eventProducer;

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
    assertThat(actual.getCodes().getFirst()).isEqualTo(codeDto);
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

  @Test
  void testDeleteCode() {
    var codeId = UUID.randomUUID();
    var code = localCode().id(codeId).buildEntity();

    when(repository.findById(codeId)).thenReturn(Optional.of(code));

    service.deleteCode(codeId);

    verify(repository).delete(code);
    verify(eventProducer).sendEvent(code.getIndicator().getField().getSpecification().getId());
  }

  @EnumSource(value = Scope.class, names = "LOCAL", mode = EnumSource.Mode.EXCLUDE)
  @ParameterizedTest
  void testDeleteCode_throwExceptionForUnsupportedScope(Scope scope) {
    var codeId = UUID.randomUUID();
    var code = new IndicatorCode();
    code.setId(codeId);
    code.setScope(scope);

    when(repository.findById(codeId)).thenReturn(Optional.of(code));

    var exception = assertThrows(ScopeModificationNotAllowedException.class, () -> service.deleteCode(codeId));
    assertThat(exception)
      .extracting(ScopeModificationNotAllowedException::getScope,
        ScopeModificationNotAllowedException::getModificationType)
      .containsExactly(scope, ScopeModificationNotAllowedException.ModificationType.DELETE);

    verifyNoMoreInteractions(repository);
    verifyNoInteractions(eventProducer);
  }

  @Test
  void testUpdateCode() {
    var codeId = UUID.randomUUID();
    var existed = localCode().id(codeId).buildEntity();
    var changeDto = new IndicatorCodeChangeDto();
    doNothing().when(mapper).update(existed, changeDto);
    when(repository.save(any())).thenReturn(existed);
    when(mapper.toDto(existed)).thenReturn(new IndicatorCodeDto());

    when(repository.findById(codeId)).thenReturn(Optional.of(existed));

    service.updateCode(codeId, changeDto);

    verify(repository).save(existed);
    verify(eventProducer).sendEvent(existed.getIndicator().getField().getSpecification().getId());
  }

  @ParameterizedTest
  @EnumSource(value = Scope.class, names = "LOCAL", mode = EnumSource.Mode.EXCLUDE)
  void testUpdateCode_throwExceptionForUnsupportedScope(Scope scope) {
    var codeId = UUID.randomUUID();
    var existed = new IndicatorCode();
    existed.setId(codeId);
    existed.setScope(scope);
    var changeDto = new IndicatorCodeChangeDto();

    when(repository.findById(codeId)).thenReturn(Optional.of(existed));

    var exception =
      assertThrows(ScopeModificationNotAllowedException.class, () -> service.updateCode(codeId, changeDto));
    assertThat(exception)
      .extracting(ScopeModificationNotAllowedException::getFieldName)
      .isEqualTo(IndicatorCode.INDICATOR_CODE_TABLE_NAME);

    verifyNoInteractions(mapper);
    verifyNoMoreInteractions(repository);
    verifyNoInteractions(eventProducer);
  }
}
