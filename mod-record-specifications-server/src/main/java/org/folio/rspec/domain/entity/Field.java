package org.folio.rspec.domain.entity;

import static org.folio.rspec.domain.entity.Field.FIELD_TABLE_NAME;
import static org.folio.rspec.domain.entity.Field.SPECIFICATION_ID_COLUMN;
import static org.folio.rspec.domain.entity.Field.TAG_COLUMN;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = FIELD_TABLE_NAME, uniqueConstraints = {
  @UniqueConstraint(name = "uc_field_tag_specification_id", columnNames = {TAG_COLUMN, SPECIFICATION_ID_COLUMN})
})
public class Field extends UuidPersistable {

  public static final String FIELD_TABLE_NAME = "field";
  public static final String TAG_COLUMN = "tag";
  public static final String LABEL_COLUMN = "label";
  public static final String URL_COLUMN = "url";
  public static final String REPEATABLE_COLUMN = "repeatable";
  public static final String REQUIRED_COLUMN = "required";
  public static final String DEPRECATED_COLUMN = "deprecated";
  public static final String SCOPE_COLUMN = "scope";
  public static final String SPECIFICATION_ID_COLUMN = "specification_id";

  @Column(name = TAG_COLUMN, nullable = false)
  private String tag;
  @Column(name = LABEL_COLUMN, nullable = false)
  private String label;
  @Column(name = URL_COLUMN)
  private String url;
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

  @ManyToOne(optional = false)
  @JoinColumn(name = SPECIFICATION_ID_COLUMN, nullable = false)
  private Specification specification;

  @OneToMany(mappedBy = "field", orphanRemoval = true)
  @OrderBy(Indicator.ORDER_COLUMN)
  private List<Indicator> indicators = new ArrayList<>();

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
