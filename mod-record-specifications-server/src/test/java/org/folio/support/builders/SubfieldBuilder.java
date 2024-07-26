package org.folio.support.builders;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.entity.Subfield;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubfieldBuilder {

  private UUID id = UUID.randomUUID();
  private UUID fieldId = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private String code = "a";
  private String label = "Default label";
  private boolean repeatable = true;
  private boolean required = true;
  private boolean deprecated = true;
  private Scope scope;

  public SubfieldBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  public SubfieldBuilder code(String code) {
    this.code = code;
    return this;
  }

  public SubfieldBuilder label(String label) {
    this.label = label;
    return this;
  }

  public SubfieldBuilder repeatable(boolean repeatable) {
    this.repeatable = repeatable;
    return this;
  }

  public SubfieldBuilder required(boolean required) {
    this.required = required;
    return this;
  }

  public SubfieldBuilder deprecated(boolean deprecated) {
    this.deprecated = deprecated;
    return this;
  }

  public Subfield buildEntity() {
    Subfield subfield = new Subfield();
    subfield.setId(id);
    subfield.setCode(code);
    subfield.setLabel(label);
    subfield.setRepeatable(repeatable);
    subfield.setRequired(required);
    subfield.setDeprecated(deprecated);
    subfield.setScope(scope);
    subfield.setField(FieldBuilder.basic().id(fieldId).buildEntity());
    return subfield;
  }
  
  public SubfieldDto buildDto() {
    SubfieldDto dto = new SubfieldDto();
    dto.setId(id);
    dto.setCode(code);
    dto.setLabel(label);
    dto.setRepeatable(repeatable);
    dto.setRequired(required);
    dto.setDeprecated(deprecated);
    dto.setScope(scope);
    dto.setFieldId(fieldId);
    return dto;
  }

  public SubfieldChangeDto buildChangeDto() {
    SubfieldChangeDto dto = new SubfieldChangeDto();
    dto.setCode(code);
    dto.setLabel(label);
    dto.setRepeatable(repeatable);
    dto.setRequired(required);
    dto.setDeprecated(deprecated);
    return dto;
  }

  public SubfieldBuilder fieldId(UUID fieldId) {
    this.fieldId = fieldId;
    return this;
  }

  public static SubfieldBuilder local() {
    var fieldBuilder = new SubfieldBuilder();
    fieldBuilder.scope = Scope.LOCAL;
    return fieldBuilder;
  }

  public static SubfieldBuilder system() {
    var fieldBuilder = new SubfieldBuilder();
    fieldBuilder.scope = Scope.SYSTEM;
    return fieldBuilder;
  }

  public static SubfieldBuilder standard() {
    var fieldBuilder = new SubfieldBuilder();
    fieldBuilder.scope = Scope.STANDARD;
    return fieldBuilder;
  }
}
