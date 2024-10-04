package org.folio.rspec.domain.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;

@Data
@JsonTypeInfo(
  use = Id.NAME,
  property = "definitionType"
)
@JsonSubTypes({
  @Type(value = SubfieldUpdateRequestEvent.class, name = "SUBFIELD")
})
public abstract class UpdateRequestEvent {

  private Family family;
  private FamilyProfile profile;
  private DefinitionType definitionType;

}
