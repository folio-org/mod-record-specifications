package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@Table(name = "specification_rule")
public class SpecificationRule {

  @EmbeddedId
  private SpecificationRuleId specificationRuleId;

  @ManyToOne
  @MapsId("specificationId")
  private Specification specification;

  @ManyToOne
  @MapsId("ruleId")
  private Rule rule;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Override
  public final int hashCode() {
    return Objects.hash(specificationRuleId);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    SpecificationRule that = (SpecificationRule) o;
    return getSpecificationRuleId() != null && Objects.equals(getSpecificationRuleId(), that.getSpecificationRuleId());
  }
}
