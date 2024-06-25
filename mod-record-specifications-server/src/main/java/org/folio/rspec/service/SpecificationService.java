package org.folio.rspec.service;

import static java.lang.Math.toIntExact;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.dto.SpecificationFieldDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.dto.ToggleSpecificationRuleDto;
import org.folio.rspec.domain.entity.Specification;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.rspec.domain.repository.SpecificationRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.mapper.SpecificationMapper;
import org.folio.rspec.service.sync.SpecificationSyncService;
import org.folio.spring.data.OffsetRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SpecificationService {

  private final SpecificationRepository specificationRepository;
  private final SpecificationMapper specificationMapper;
  private final SpecificationRuleService specificationRuleService;
  private final SpecificationFieldService specificationFieldService;
  private final SpecificationSyncService specificationSyncService;

  @Transactional
  public SpecificationDtoCollection findSpecifications(Family family, FamilyProfile profile, IncludeParam include,
                                                       Integer limit, Integer offset) {
    log.debug("findSpecifications::family={}, profile={}, include={}, limit={}, offset={}",
      family, profile, include, limit, offset);
    var specificationCollection = new SpecificationDtoCollection();

    var page = specificationRepository.findByFamilyAndProfile(family, profile, OffsetRequest.of(offset, limit));

    page.map(specification -> switch (include) {
      case ALL -> specificationMapper.toFullDto(specification);
      default -> specificationMapper.toDto(specification);
    }).forEach(specificationCollection::addSpecificationsItem);

    return specificationCollection.totalRecords(toIntExact(page.getTotalElements()));
  }

  @Transactional
  public SpecificationRuleDtoCollection findSpecificationRules(UUID specificationId) {
    log.debug("findSpecificationRules::specificationId={}", specificationId);
    return doForSpecificationOrFail(specificationId,
      specification -> specificationRuleService.findSpecificationRules(specificationId)
    );
  }

  public void toggleSpecificationRule(UUID specificationId, UUID ruleId,
                                      ToggleSpecificationRuleDto toggleSpecificationRuleDto) {
    log.debug("toggleSpecificationRule::specificationId={}, ruleId={}", specificationId, toggleSpecificationRuleDto);
    Objects.requireNonNull(toggleSpecificationRuleDto.getEnabled());
    specificationRuleService.toggleSpecificationRule(new SpecificationRuleId(specificationId, ruleId),
      toggleSpecificationRuleDto.getEnabled());
  }

  @Transactional
  public SpecificationFieldDtoCollection findSpecificationFields(UUID specificationId) {
    log.debug("findSpecificationFields::specificationId={}", specificationId);
    return doForSpecificationOrFail(specificationId,
      specification -> specificationFieldService.findSpecificationFields(specificationId)
    );
  }

  @Transactional
  public SpecificationFieldDto createLocalField(UUID specificationId, SpecificationFieldChangeDto createDto) {
    log.debug("createLocalField::specificationId={}, createDto={}", specificationId, createDto);
    return doForSpecificationOrFail(specificationId,
      specification -> specificationFieldService.createLocalField(specification, createDto)
    );
  }

  public void sync(UUID specificationId) {
    log.info("sync::specificationId={}", specificationId);
    var specification = doForSpecificationOrFail(specificationId, Function.identity());
    specificationSyncService.sync(specification);
  }

  private <T> T doForSpecificationOrFail(UUID specificationId, Function<Specification, T> action) {
    return specificationRepository.findById(specificationId)
      .map(action)
      .orElseThrow(() -> ResourceNotFoundException.forSpecification(specificationId));
  }
}
