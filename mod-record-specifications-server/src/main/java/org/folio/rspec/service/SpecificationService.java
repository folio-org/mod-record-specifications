package org.folio.rspec.service;

import static java.lang.Math.toIntExact;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.mapper.SpecificationMapper;
import org.folio.spring.data.OffsetRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpecificationService {

  private final SpecificationRepository specificationRepository;
  private final SpecificationMapper specificationMapper;
  private final SpecificationRuleService specificationRuleService;

  @Transactional
  public SpecificationDtoCollection findSpecifications(Family family, FamilyProfile profile, IncludeParam include,
                                                       Integer limit, Integer offset) {
    var specificationCollection = new SpecificationDtoCollection();

    var page = specificationRepository.findByFamilyAndProfile(family, profile, OffsetRequest.of(offset, limit));

    page.map(specification -> switch (include) {
      case ALL -> specificationMapper.toFullDto(specification);
      default -> specificationMapper.toDto(specification);
    }).forEach(specificationCollection::addSpecificationsItem);

    return specificationCollection.totalRecords(toIntExact(page.getTotalElements()));
  }

  @Transactional
  public SpecificationRuleDtoCollection findSpecificationRules(UUID id) {
    return specificationRepository.findById(id)
      .map(specification -> specificationRuleService.findSpecificationRules(id))
      .orElseThrow(() -> ResourceNotFoundException.forSpecification(id));
  }

  public void toggleSpecificationRule(UUID specificationId, UUID ruleId,
                                      ToggleSpecificationRuleDto toggleSpecificationRuleDto) {
    Objects.requireNonNull(toggleSpecificationRuleDto.getEnabled());
    specificationRuleService.toggleSpecificationRule(new SpecificationRuleId(specificationId, ruleId),
      toggleSpecificationRuleDto.getEnabled());
  }

}
