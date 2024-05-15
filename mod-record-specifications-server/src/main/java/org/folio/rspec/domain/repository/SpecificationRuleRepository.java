package org.folio.rspec.domain.repository;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.SpecificationRule;
import org.folio.rspec.domain.entity.SpecificationRuleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SpecificationRuleRepository extends JpaRepository<SpecificationRule, SpecificationRuleId> {

  @Query("""
    select s, r from SpecificationRule s
    join Rule r on s.specificationRuleId.ruleId = r.id
    where s.specificationRuleId.specificationId = ?1
    """)
  List<SpecificationRule> findBySpecificationId(UUID specificationId);

  @Transactional
  @Modifying
  @Query("update SpecificationRule s set s.enabled = ?1 where s.specificationRuleId = ?2")
  int updateEnabledBySpecificationRuleId(boolean enabled, SpecificationRuleId specificationRuleId);

}
