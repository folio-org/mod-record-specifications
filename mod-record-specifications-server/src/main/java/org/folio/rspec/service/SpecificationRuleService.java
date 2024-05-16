package org.folio.rspec.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.rspec.domain.repository.SpecificationRuleRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.mapper.SpecificationRuleMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecificationRuleService {

  private final SpecificationRuleRepository specificationRuleRepository;
  private final SpecificationRuleMapper specificationRuleMapper;

  public SpecificationRuleDtoCollection findSpecificationRules(UUID specificationId) {
    var specificationRuleCollection = new SpecificationRuleDtoCollection();

    var specificationRules = specificationRuleRepository.findBySpecificationId(specificationId)
      .stream()
      .map(specificationRuleMapper::toDto)
      .toList();

    return specificationRuleCollection.rules(specificationRules).totalRecords(specificationRules.size());
  }

  public void toggleSpecificationRule(SpecificationRuleId specificationRuleId, boolean enabled) {
    int updated = specificationRuleRepository.updateEnabledBySpecificationRuleId(enabled, specificationRuleId);
    if (updated == 0) {
      throw ResourceNotFoundException.forSpecificationRule(specificationRuleId);
    }
  }
}
