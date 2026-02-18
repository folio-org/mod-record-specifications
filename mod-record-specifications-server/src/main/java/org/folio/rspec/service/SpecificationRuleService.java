package org.folio.rspec.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.folio.rspec.domain.repository.SpecificationRuleRepository;
import org.folio.rspec.exception.ResourceNotFoundException;
import org.folio.rspec.service.mapper.SpecificationRuleMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SpecificationRuleService {

  private final SpecificationRuleRepository specificationRuleRepository;
  private final SpecificationRuleMapper specificationRuleMapper;

  public SpecificationRuleDtoCollection findSpecificationRules(UUID specificationId) {
    log.debug("findSpecificationRules::specificationId={}", specificationId);
    var specificationRuleCollection = new SpecificationRuleDtoCollection();

    var specificationRules = specificationRuleRepository.findBySpecificationId(specificationId)
      .stream()
      .map(specificationRuleMapper::toDto)
      .toList();

    return specificationRuleCollection.rules(specificationRules).totalRecords(specificationRules.size());
  }

  public void toggleSpecificationRule(SpecificationRuleId specificationRuleId, boolean enabled) {
    log.info("toggleSpecificationRule::specificationRuleId={}, enabled={}", specificationRuleId, enabled);
    var specificationRule = specificationRuleRepository.findById(specificationRuleId)
      .orElseThrow(() -> ResourceNotFoundException.forSpecificationRule(specificationRuleId));
    specificationRule.setEnabled(enabled);
    specificationRuleRepository.save(specificationRule);
  }
}
