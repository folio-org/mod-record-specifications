package org.folio.support.builders;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SpecificationFieldChangeDto;
import org.folio.rspec.domain.dto.SpecificationFieldDto;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.entity.Specification;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldBuilder {

  private UUID id = UUID.randomUUID();
  private UUID specificationId = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private String tag = "100";
  private String label = "Default label";
  private String url = "http://www.example.com";
  private boolean repeatable = true;
  private boolean required = true;
  private boolean deprecated = true;
  private Scope scope;

  public FieldBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  public FieldBuilder tag(String tag) {
    this.tag = tag;
    return this;
  }

  public FieldBuilder label(String label) {
    this.label = label;
    return this;
  }

  public FieldBuilder url(String url) {
    this.url = url;
    return this;
  }

  public FieldBuilder repeatable(boolean repeatable) {
    this.repeatable = repeatable;
    return this;
  }

  public FieldBuilder required(boolean required) {
    this.required = required;
    return this;
  }

  public FieldBuilder deprecated(boolean deprecated) {
    this.deprecated = deprecated;
    return this;
  }

  public Field buildEntity() {
    Field field = new Field();
    field.setId(id);
    field.setTag(tag);
    field.setLabel(label);
    field.setUrl(url);
    field.setRepeatable(repeatable);
    field.setRequired(required);
    field.setDeprecated(deprecated);
    field.setScope(scope);
    var specification = new Specification();
    specification.setId(specificationId);
    field.setSpecification(specification);
    return field;
  }
  
  public SpecificationFieldDto buildDto() {
    SpecificationFieldDto dto = new SpecificationFieldDto();
    dto.setId(id);
    dto.setTag(tag);
    dto.setLabel(label);
    dto.setUrl(url);
    dto.setRepeatable(repeatable);
    dto.setRequired(required);
    dto.setDeprecated(deprecated);
    dto.setScope(scope);
    dto.setSpecificationId(specificationId);
    return dto;
  }

  public SpecificationFieldChangeDto buildChangeDto() {
    SpecificationFieldChangeDto dto = new SpecificationFieldChangeDto();
    dto.setTag(tag);
    dto.setLabel(label);
    dto.setUrl(url);
    dto.setRepeatable(repeatable);
    dto.setRequired(required);
    dto.setDeprecated(deprecated);
    return dto;
  }

  public FieldBuilder specificationId(UUID specificationId) {
    this.specificationId = specificationId;
    return this;
  }

  public static FieldBuilder local() {
    var fieldBuilder = new FieldBuilder();
    fieldBuilder.scope = Scope.LOCAL;
    return fieldBuilder;
  }

  public static FieldBuilder system() {
    var fieldBuilder = new FieldBuilder();
    fieldBuilder.scope = Scope.SYSTEM;
    return fieldBuilder;
  }

  public static FieldBuilder standard() {
    var fieldBuilder = new FieldBuilder();
    fieldBuilder.scope = Scope.STANDARD;
    return fieldBuilder;
  }
}
