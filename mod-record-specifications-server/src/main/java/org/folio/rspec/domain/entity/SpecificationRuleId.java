package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class SpecificationRuleId implements Serializable {

  public static final String SPECIFICATION_ID_COLUMN = "specification_id";
  public static final String RULE_ID_COLUMN = "rule_id";

  @Column(name = SPECIFICATION_ID_COLUMN)
  private UUID specificationId;
  @Column(name = RULE_ID_COLUMN)
  private UUID ruleId;

  @Override
  public int hashCode() {
    return Objects.hash(specificationId, ruleId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    SpecificationRuleId entity = (SpecificationRuleId) o;
    return Objects.equals(this.specificationId, entity.specificationId)
      && Objects.equals(this.ruleId, entity.ruleId);
  }

  @Override
  public String toString() {
    return "specificationId=" + specificationId + ", ruleId=" + ruleId;
  }
}
