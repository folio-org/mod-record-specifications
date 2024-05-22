package org.folio.rspec.exception;

import lombok.Getter;

/**
 * This class represents a custom exception indicating that a resource was not found.
 * The exception message is constructed using a resource name and ID.
 */
public class ResourceNotFoundException extends RuntimeException {

  private static final String MSG_TEMPLATE = "%s with ID [%s] was not found";

  protected ResourceNotFoundException(Resource resource, Object id) {
    super(String.format(MSG_TEMPLATE, resource.getName(), id));
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

  @Getter
  protected enum Resource {

    SPECIFICATION("specification"),
    SPECIFICATION_RULE("specification rule"),
    FIELD_DEFINITION("field definition");

    private final String name;

    Resource(String name) {
      this.name = name;
    }
  }
}
