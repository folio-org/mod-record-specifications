package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.folio.rspec.domain.entity.metadata.FieldMetadata;
import org.folio.rspec.domain.entity.support.UuidPersistable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "specification_metadata")
public class SpecificationMetadata extends UuidPersistable {

  @OneToOne(orphanRemoval = true)
  @JoinColumn(name = "specification_id")
  private Specification specification;

  @Column(name = "sync_url")
  private String syncUrl;

  @Column(name = "url_format")
  private String urlFormat;

  @Column(name = "fields")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, FieldMetadata> fields;

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
