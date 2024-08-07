package org.folio.rspec.domain.entity;

import static org.folio.rspec.domain.entity.Indicator.FIELD_ID_COLUMN;
import static org.folio.rspec.domain.entity.Indicator.INDICATOR_TABLE_NAME;
import static org.folio.rspec.domain.entity.Indicator.ORDER_COLUMN;

import jakarta.persistence.CascadeType;
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
  @UniqueConstraint(name = Indicator.ORDER_UNIQUE_CONSTRAINT, columnNames = {ORDER_COLUMN, FIELD_ID_COLUMN})
})
public class Indicator extends UuidPersistable {

  public static final String INDICATOR_TABLE_NAME = "indicator";
  public static final String ORDER_COLUMN = "indicator_order";
  public static final String LABEL_COLUMN = "label";
  public static final String FIELD_ID_COLUMN = "field_id";
  public static final String ORDER_UNIQUE_CONSTRAINT = "uc_indicator_order_field_id";

  @Column(name = ORDER_COLUMN)
  private Integer order;
  @Column(name = LABEL_COLUMN, nullable = false)
  private String label;

  @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
  @JoinColumn(name = FIELD_ID_COLUMN, nullable = false)
  private Field field;

  @OneToMany(mappedBy = INDICATOR_TABLE_NAME, orphanRemoval = true, cascade = {CascadeType.ALL})
  @OrderBy(IndicatorCode.CODE_COLUMN)
  private List<IndicatorCode> codes = new ArrayList<>();

  @Embedded
  private Metadata metadata = new Metadata();

  public void setCodes(List<IndicatorCode> codes) {
    codes.forEach(code -> code.setIndicator(this));
    this.codes = codes;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
