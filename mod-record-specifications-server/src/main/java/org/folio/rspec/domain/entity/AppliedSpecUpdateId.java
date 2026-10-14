package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Composite key for {@link AppliedSpecUpdate}: a ticket's code is applied once per concrete
 * family/profile it actually touched, so re-applying an update whose scope has since changed
 * records an additional row instead of overwriting the earlier one - every scope an update was
 * ever applied with for this tenant stays in the table as a durable record, not just the latest.
 */
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class AppliedSpecUpdateId implements Serializable {

  public static final String CODE_COLUMN = "code";
  public static final String FAMILY_COLUMN = "family";
  public static final String PROFILE_COLUMN = "profile";

  @Column(name = CODE_COLUMN)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(name = FAMILY_COLUMN, columnDefinition = "family_enum")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private Family family;

  @Enumerated(EnumType.STRING)
  @Column(name = PROFILE_COLUMN, columnDefinition = "family_profile_enum")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private FamilyProfile profile;

  @Override
  public int hashCode() {
    return Objects.hash(code, family, profile);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    AppliedSpecUpdateId entity = (AppliedSpecUpdateId) o;
    return Objects.equals(this.code, entity.code)
      && this.family == entity.family
      && this.profile == entity.profile;
  }

  @Override
  public String toString() {
    return "code=" + code + ", family=" + family + ", profile=" + profile;
  }
}
