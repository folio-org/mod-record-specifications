package org.folio.rspec.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class represents a custom exception indicating that a resource was not found.
 * The exception message is constructed using a resource name and ID.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

  private static final String MSG_BY_ID_TEMPLATE = "%s with ID [%s] was not found";
  private static final String MSG_BY_PARAMS_TEMPLATE = "%s with params [%s] was not found";

  private final transient Resource resource;
  private final transient Object id;
  private final transient List<Pair<String, Object>> searchableParams;

  protected ResourceNotFoundException(Resource resource, Object id) {
    super(String.format(MSG_BY_ID_TEMPLATE, resource.getName(), id));
    this.resource = resource;
    this.id = id;
    this.searchableParams = new ArrayList<>();
  }

  protected ResourceNotFoundException(Resource resource, List<Pair<String, Object>> searchableParams) {
    super(String.format(MSG_BY_PARAMS_TEMPLATE, resource.getName(), searchableParams));
    this.resource = resource;
    this.searchableParams = searchableParams;
    this.id = null;
  }

  public static ResourceNotFoundException forSpecification(Object id) {
    return new ResourceNotFoundException(Resource.SPECIFICATION, id);
  }

  public static ResourceNotFoundException forSpecificationRule(Object id) {
    return new ResourceNotFoundException(Resource.SPECIFICATION_RULE, id);
  }

  public static ResourceNotFoundException forField(Object id) {
    return new ResourceNotFoundException(Resource.FIELD_DEFINITION, id);
  }

  public static ResourceNotFoundException forField(UUID specificationId, String fieldTag) {
    return new ResourceNotFoundException(Resource.FIELD_DEFINITION, List.of(
      Pair.of("specificationId", specificationId),
      Pair.of("tag", fieldTag)
    ));
  }

  public static ResourceNotFoundException forIndicator(Object id) {
    return new ResourceNotFoundException(Resource.FIELD_INDICATOR, id);
  }

  public static ResourceNotFoundException forIndicatorCode(Object id) {
    return new ResourceNotFoundException(Resource.INDICATOR_CODE, id);
  }

  public static ResourceNotFoundException forSubfield(Object id) {
    return new ResourceNotFoundException(Resource.SUBFIELD, id);
  }

  @Getter
  public enum Resource {

    SPECIFICATION("specification"),
    SPECIFICATION_RULE("specification rule"),
    FIELD_DEFINITION("field definition"),
    FIELD_INDICATOR("field indicator"),
    INDICATOR_CODE("indicator code"),
    SUBFIELD("subfield");

    private final String name;

    Resource(String name) {
      this.name = name;
    }
  }
}
