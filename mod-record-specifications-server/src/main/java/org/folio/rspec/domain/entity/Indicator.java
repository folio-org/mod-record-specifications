package org.folio.rspec.domain.entity;

import static org.folio.rspec.domain.entity.Indicator.FIELD_ID_COLUMN;
import static org.folio.rspec.domain.entity.Indicator.INDICATOR_TABLE_NAME;
import static org.folio.rspec.domain.entity.Indicator.ORDER_COLUMN;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.rspec.domain.entity.support.UuidPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = INDICATOR_TABLE_NAME, uniqueConstraints = {
  @UniqueConstraint(name = "uc_indicator_order_field_id", columnNames = {ORDER_COLUMN, FIELD_ID_COLUMN})
})
public class Indicator extends UuidPersistable {

  public static final String INDICATOR_TABLE_NAME = "indicator";
  public static final String ORDER_COLUMN = "indicator_order";
  public static final String LABEL_COLUMN = "label";
  public static final String FIELD_ID_COLUMN = "field_id";

  @Column(name = ORDER_COLUMN)
  private Integer order;
  @Column(name = LABEL_COLUMN, nullable = false)
  private String label;

  @ManyToOne(optional = false)
  @JoinColumn(name = FIELD_ID_COLUMN, nullable = false)
  private Field field;

  @OneToMany(mappedBy = "indicator", orphanRemoval = true)
  @OrderBy(IndicatorCode.CODE_COLUMN)
  private List<IndicatorCode> codes = new ArrayList<>();

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
