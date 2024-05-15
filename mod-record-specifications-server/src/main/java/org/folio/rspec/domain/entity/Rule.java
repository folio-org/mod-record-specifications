package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.rspec.domain.entity.support.UuidPersistable;

@Getter
@Setter
@Entity
@Table(name = "rule")
public class Rule extends UuidPersistable {

  public static final String NAME_COLUMN = "name";
  public static final String DESCRIPTION_COLUMN = "description";
  public static final String CODE_COLUMN = "code";

  @Column(name = NAME_COLUMN, nullable = false)
  private String name;
  @Column(name = DESCRIPTION_COLUMN)
  private String description;
  @Column(name = CODE_COLUMN, nullable = false)
  private String code;

  @Embedded
  private Metadata metadata;

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
}
