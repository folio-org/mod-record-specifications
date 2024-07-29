package org.folio.rspec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.builders.FieldBuilder.local;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.integration.kafka.EventProducer;
import org.folio.rspec.service.mapper.SpecificationMapper;
import org.folio.rspec.service.sync.SpecificationSyncService;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith({MockitoExtension.class, RandomParametersExtension.class})
class SpecificationServiceTest {

  @InjectMocks
  private SpecificationService service;

  @Mock
  private SpecificationRepository repository;

  @Mock
  private SpecificationMapper mapper;

  @Mock
  private SpecificationRuleService ruleService;

  @Mock
  private SpecificationFieldService fieldService;

  @Mock
  private SpecificationSyncService syncService;

  @Mock
  private EventProducer<UUID, SpecificationUpdatedEvent> eventProducer;

  @Test
  void testFindSpecifications(@Random SpecificationDto specificationDto) {
    var family = Family.MARC;
    var profile = FamilyProfile.AUTHORITY;
    var page = new PageImpl<>(List.of(new Specification()));

    when(repository.findByFamilyAndProfile(family, profile, OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toDto(any(Specification.class))).thenReturn(specificationDto);

    service.findSpecifications(family, profile, IncludeParam.NONE, 10, 0);
    verify(mapper).toDto(any(Specification.class));
  }

  @Test
  void testFindSpecifications_withIncludeAll(@Random SpecificationDto specificationDto) {
    var family = Family.MARC;
    var profile = FamilyProfile.AUTHORITY;
    var page = new PageImpl<>(List.of(new Specification()));

    when(repository.findByFamilyAndProfile(family, profile, OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toFullDto(any(Specification.class))).thenReturn(specificationDto);

    service.findSpecifications(family, profile, IncludeParam.ALL, 10, 0);
    verify(mapper).toFullDto(any(Specification.class));
  }

  @Test
  void testFindSpecificationById(@Random UUID specificationId, @Random SpecificationDto specificationDto) {
    var specification = new Specification();

    when(repository.findById(specificationId)).thenReturn(Optional.of(specification));
    when(mapper.toDto(any(Specification.class))).thenReturn(specificationDto);

    var actual = service.findSpecificationById(specificationId, IncludeParam.NONE);

    assertThat(actual).isEqualTo(specificationDto);
    verify(mapper).toDto(any(Specification.class));
  }

  @Test
  void testFindSpecificationById_withIncludeAll(@Random UUID specificationId,
                                                @Random SpecificationDto specificationDto) {
    var specification = new Specification();

    when(repository.findById(specificationId)).thenReturn(Optional.of(specification));
    when(mapper.toFullDto(any(Specification.class))).thenReturn(specificationDto);

    var actual = service.findSpecificationById(specificationId, IncludeParam.ALL);

    assertThat(actual).isEqualTo(specificationDto);
    verify(mapper).toFullDto(any(Specification.class));
  }

  @Test
  void testFindSpecificationById_notFound(@Random UUID specificationId) {
    var exception = Assertions.assertThrows(ResourceNotFoundException.class,
      () -> service.findSpecificationById(specificationId, IncludeParam.ALL));

    assertThat(exception.getId()).isEqualTo(specificationId);
    assertThat(exception.getResource()).isEqualTo(ResourceNotFoundException.Resource.SPECIFICATION);
    verifyNoInteractions(mapper);
  }

  @Test
  void testFindSpecificationRules() {
    var id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.of(new Specification()));
    when(ruleService.findSpecificationRules(id)).thenReturn(new SpecificationRuleDtoCollection());

    service.findSpecificationRules(id);
    verify(ruleService).findSpecificationRules(id);
  }

  @Test
  void testToggleSpecificationRule() {
    var specificationId = UUID.randomUUID();
    var ruleId = UUID.randomUUID();
    var toggleSpecificationRuleDto = new ToggleSpecificationRuleDto(Boolean.TRUE);

    service.toggleSpecificationRule(specificationId, ruleId, toggleSpecificationRuleDto);
    verify(ruleService).toggleSpecificationRule(new SpecificationRuleId(specificationId, ruleId), true);
    verify(eventProducer).sendEvent(specificationId);
  }

  @Test
  void testCreateLocalField() {
    var specificationId = UUID.randomUUID();
    var createDto = local().buildChangeDto();
    var field = local().buildDto();
    when(repository.findById(specificationId)).thenReturn(Optional.of(new Specification()));
    when(fieldService.createLocalField(any(), eq(createDto))).thenReturn(field);

    var actual = service.createLocalField(specificationId, createDto);

    assertThat(actual).isEqualTo(field);
    verify(eventProducer).sendEvent(specificationId);
  }

  @Test
  void testSync() {
    var specificationId = UUID.randomUUID();
    var specification = new Specification();
    when(repository.findById(specificationId)).thenReturn(Optional.of(specification));

    service.sync(specificationId);

    verify(syncService).sync(specification);
    verify(eventProducer).sendEvent(specificationId);
  }
}
