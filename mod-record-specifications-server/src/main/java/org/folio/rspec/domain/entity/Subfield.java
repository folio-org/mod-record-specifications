package org.folio.rspec.domain.entity;

import static org.folio.rspec.domain.entity.Subfield.CODE_COLUMN;
import static org.folio.rspec.domain.entity.Subfield.FIELD_ID_COLUMN;
import static org.folio.rspec.domain.entity.Subfield.SUBFIELD_CODE_UNIQUE_CONSTRAINT;
import static org.folio.rspec.domain.entity.Subfield.SUBFIELD_TABLE_NAME;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.rspec.domain.entity.support.UuidPersistable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = SUBFIELD_TABLE_NAME, uniqueConstraints = {
  @UniqueConstraint(name = SUBFIELD_CODE_UNIQUE_CONSTRAINT, columnNames = {CODE_COLUMN, FIELD_ID_COLUMN})
})
public class Subfield extends UuidPersistable {

  public static final String SUBFIELD_TABLE_NAME = "subfield";

  public static final String CODE_COLUMN = "code";
  public static final String LABEL_COLUMN = "label";
  public static final String REPEATABLE_COLUMN = "repeatable";
  public static final String REQUIRED_COLUMN = "required";
  public static final String DEPRECATED_COLUMN = "deprecated";
  public static final String SCOPE_COLUMN = "scope";
  public static final String FIELD_ID_COLUMN = "field_id";

  public static final String SUBFIELD_CODE_UNIQUE_CONSTRAINT = "uc_subfield_code_field_id";

  @Column(name = CODE_COLUMN, nullable = false, length = 1)
  private String code;
  @Column(name = LABEL_COLUMN, nullable = false, length = 350)
  private String label;
  @Column(name = REPEATABLE_COLUMN, nullable = false)
  private boolean repeatable = true;
  @Column(name = REQUIRED_COLUMN, nullable = false)
  private boolean required = false;
  @Column(name = DEPRECATED_COLUMN, nullable = false)
  private boolean deprecated = false;

  @Enumerated(EnumType.STRING)
  @Column(name = SCOPE_COLUMN, nullable = false, columnDefinition = "scope_enum")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private Scope scope;

  @Embedded
  private Metadata metadata = new Metadata();

  @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = FIELD_ID_COLUMN, nullable = false)
  private Field field;

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
