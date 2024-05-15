package org.folio.rspec.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationFullDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.service.mapper.SpecificationMapper;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
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
  void testFindSpecifications_withIncludeAll(@Random SpecificationFullDto specificationDto) {
    var family = Family.MARC;
    var profile = FamilyProfile.AUTHORITY;
    var page = new PageImpl<>(List.of(new Specification()));

    when(repository.findByFamilyAndProfile(family, profile, OffsetRequest.of(0, 10))).thenReturn(page);
    when(mapper.toFullDto(any(Specification.class))).thenReturn(specificationDto);

    service.findSpecifications(family, profile, IncludeParam.ALL, 10, 0);
    verify(mapper).toFullDto(any(Specification.class));
  }

  @Test
  void testFindSpecificationRules() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.of(new Specification()));
    when(ruleService.findSpecificationRules(id)).thenReturn(new SpecificationRuleDtoCollection());

    service.findSpecificationRules(id);
    verify(ruleService).findSpecificationRules(id);
  }

  @Test
  void testToggleSpecificationRule() {
    UUID specificationId = UUID.randomUUID();
    UUID ruleId = UUID.randomUUID();
    ToggleSpecificationRuleDto toggleSpecificationRuleDto = new ToggleSpecificationRuleDto(Boolean.TRUE);

    service.toggleSpecificationRule(specificationId, ruleId, toggleSpecificationRuleDto);
    verify(ruleService).toggleSpecificationRule(new SpecificationRuleId(specificationId, ruleId), true);
  }
}
