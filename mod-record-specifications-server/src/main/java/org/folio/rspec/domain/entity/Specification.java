package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.rspec.domain.entity.support.UuidPersistable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "specification", uniqueConstraints = {
  @UniqueConstraint(name = "uc_specification_family_profile",
                    columnNames = {Specification.FAMILY_COLUMN, Specification.PROFILE_COLUMN})
})
@EntityListeners(AuditingEntityListener.class)
public class Specification extends UuidPersistable {

  public static final String TITLE_COLUMN = "title";
  public static final String FAMILY_COLUMN = "family";
  public static final String PROFILE_COLUMN = "profile";
  public static final String URL_COLUMN = "url";

  @Column(name = TITLE_COLUMN, nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = FAMILY_COLUMN, nullable = false, columnDefinition = "family_enum")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private Family family;

  @Enumerated(EnumType.STRING)
  @Column(name = PROFILE_COLUMN, nullable = false, columnDefinition = "family_profile_enum")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private FamilyProfile profile;

  @Column(name = URL_COLUMN)
  private String url;

  @OneToMany(mappedBy = "specification", orphanRemoval = true)
  private Set<SpecificationRule> specificationRules = new LinkedHashSet<>();

  @Embedded
  private Metadata metadata;

  @OneToMany(mappedBy = "specification", orphanRemoval = true)
  @OrderBy(Field.TAG_COLUMN)
  private List<Field> fields = new ArrayList<>();

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
}
