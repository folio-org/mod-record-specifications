package org.folio.support.builders;

import static org.folio.support.builders.FieldBuilder.local;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.folio.rspec.domain.dto.FieldIndicatorChangeDto;
import org.folio.rspec.domain.dto.FieldIndicatorDto;
import org.folio.rspec.domain.entity.Indicator;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndicatorBuilder {

  private UUID id = UUID.randomUUID();
  private UUID fieldId = UUID.fromString("22222222-2222-2222-2222-222222222223");
  private Integer order = 1;
  private String label = "Ind 1";

  public IndicatorBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  public IndicatorBuilder order(Integer order) {
    this.order = order;
    return this;
  }

  public IndicatorBuilder label(String label) {
    this.label = label;
    return this;
  }

  public IndicatorBuilder fieldId(UUID fieldId) {
    this.fieldId = fieldId;
    return this;
  }

  public Indicator buildEntity() {
    var indicator = new Indicator();
    indicator.setId(id);
    indicator.setOrder(order);
    indicator.setLabel(label);
    var field = local().id(fieldId).buildEntity();
    indicator.setField(field);
    return indicator;
  }
  
  public FieldIndicatorDto buildDto() {
    var dto = new FieldIndicatorDto();
    dto.setId(id);
    dto.setOrder(order);
    dto.setLabel(label);
    dto.setFieldId(fieldId);
    return dto;
  }

  public FieldIndicatorChangeDto buildChangeDto() {
    var dto = new FieldIndicatorChangeDto();
    dto.setOrder(order);
    dto.setLabel(label);
    return dto;
  }

  public static IndicatorBuilder basic() {
    return new IndicatorBuilder();
  }
}
