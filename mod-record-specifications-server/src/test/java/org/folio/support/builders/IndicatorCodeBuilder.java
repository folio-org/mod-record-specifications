package org.folio.support.builders;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.folio.rspec.domain.dto.IndicatorCodeChangeDto;
import org.folio.rspec.domain.dto.IndicatorCodeDto;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.entity.Indicator;
import org.folio.rspec.domain.entity.IndicatorCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndicatorCodeBuilder {

  private UUID id = UUID.randomUUID();
  private UUID indicatorId = UUID.fromString("22222222-2222-2222-2222-222222222224");
  private String code = "a";
  private String label = "Somw label";
  private Scope scope = Scope.LOCAL;
  private Boolean deprecated = false;

  public IndicatorCodeBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  public IndicatorCodeBuilder code(String code) {
    this.code = code;
    return this;
  }

  public IndicatorCodeBuilder label(String label) {
    this.label = label;
    return this;
  }

  public IndicatorCodeBuilder scope(Scope scope) {
    this.scope = scope;
    return this;
  }

  public IndicatorCodeBuilder deprecated(Boolean deprecated) {
    this.deprecated = deprecated;
    return this;
  }

  public IndicatorCodeBuilder indicatorId(UUID indicatorId) {
    this.indicatorId = indicatorId;
    return this;
  }

  public IndicatorCode buildEntity() {
    var indicatorCode = new IndicatorCode();
    indicatorCode.setId(id);
    indicatorCode.setCode(code);
    indicatorCode.setLabel(label);
    indicatorCode.setScope(scope);
    indicatorCode.setDeprecated(deprecated);
    var indicator = new Indicator();
    indicator.setId(indicatorId);
    indicatorCode.setIndicator(indicator);
    return indicatorCode;
  }
  
  public IndicatorCodeDto buildDto() {
    var dto = new IndicatorCodeDto();
    dto.setId(id);
    dto.setCode(code);
    dto.setLabel(label);
    dto.setScope(scope);
    dto.setDeprecated(deprecated);
    dto.setIndicatorId(indicatorId);
    return dto;
  }

  public IndicatorCodeChangeDto buildChangeDto() {
    var dto = new IndicatorCodeChangeDto();
    dto.setCode(code);
    dto.setLabel(label);
    dto.setDeprecated(deprecated);
    return dto;
  }

  public static IndicatorCodeBuilder localCode() {
    var codeBuilder = new IndicatorCodeBuilder();
    codeBuilder.scope = Scope.LOCAL;
    return codeBuilder;
  }

  public static IndicatorCodeBuilder standardCode() {
    var codeBuilder = new IndicatorCodeBuilder();
    codeBuilder.scope = Scope.STANDARD;
    return codeBuilder;
  }

  public static IndicatorCodeBuilder systemCode() {
    var codeBuilder = new IndicatorCodeBuilder();
    codeBuilder.scope = Scope.SYSTEM;
    return codeBuilder;
  }
}
