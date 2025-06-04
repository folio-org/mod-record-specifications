package org.folio.rspec.domain.entity;

import static org.folio.rspec.domain.entity.IndicatorCode.CODE_COLUMN;
import static org.folio.rspec.domain.entity.IndicatorCode.INDICATOR_CODE_TABLE_NAME;
import static org.folio.rspec.domain.entity.IndicatorCode.INDICATOR_ID_COLUMN;

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
@Table(name = INDICATOR_CODE_TABLE_NAME, uniqueConstraints = {
  @UniqueConstraint(name = IndicatorCode.CODE_UNIQUE_CONSTRAINT, columnNames = {CODE_COLUMN, INDICATOR_ID_COLUMN})
})
public class IndicatorCode extends UuidPersistable {

  public static final String INDICATOR_CODE_TABLE_NAME = "indicator_code";
  public static final String CODE_COLUMN = "code";
  public static final String LABEL_COLUMN = "label";
  public static final String SCOPE_COLUMN = "scope";
  public static final String DEPRECATED_COLUMN = "deprecated";
  public static final String INDICATOR_ID_COLUMN = "indicator_id";
  public static final String CODE_UNIQUE_CONSTRAINT = "uc_indicator_code_indicator_id";

  @Column(name = CODE_COLUMN)
  private String code;
  @Column(name = LABEL_COLUMN, nullable = false)
  private String label;
  @Column(name = DEPRECATED_COLUMN)
  private boolean deprecated = false;

  @Enumerated(EnumType.STRING)
  @Column(name = SCOPE_COLUMN, nullable = false, columnDefinition = "scope_enum")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private Scope scope;

  @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
  @JoinColumn(name = INDICATOR_ID_COLUMN, nullable = false)
  private Indicator indicator;

  @Embedded
  private Metadata metadata = new Metadata();

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
