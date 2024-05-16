package org.folio.rspec.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class RuleTest {

  private Rule rule;

  @BeforeEach
  void setUp() {
    rule = new Rule();
    rule.setId(UUID.randomUUID());
  }

  @Test
  void testGettersAndSetters() {
    UUID id = UUID.randomUUID();
    rule.setId(id);
    rule.setName("foo");
    rule.setDescription("bar");
    rule.setCode("baz");
    var metadata = new Metadata();
    rule.setMetadata(metadata);

    assertEquals(id, rule.getId());
    assertEquals("foo", rule.getName());
    assertEquals("bar", rule.getDescription());
    assertEquals("baz", rule.getCode());
    assertEquals(metadata, rule.getMetadata());
  }

  @Test
  void testEqualsAndHashCode() {
    Rule rule2 = new Rule();
    rule2.setId(rule.getId());

    assertEquals(rule, rule2);
    assertEquals(rule.hashCode(), rule2.hashCode());
  }
}
