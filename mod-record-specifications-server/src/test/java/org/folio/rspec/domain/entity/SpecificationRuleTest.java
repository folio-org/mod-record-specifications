package org.folio.rspec.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class SpecificationRuleTest {

  @Test
  void testEqualsAndHashCode() {
    SpecificationRule obj1 = new SpecificationRule();
    SpecificationRule obj2 = new SpecificationRule();
    SpecificationRuleId id = new SpecificationRuleId(UUID.randomUUID(), UUID.randomUUID());

    obj1.setSpecificationRuleId(id);
    obj2.setSpecificationRuleId(id);

    assertTrue(obj1.equals(obj2) && obj2.equals(obj1));
    assertEquals(obj1.hashCode(), obj2.hashCode());
  }

  @Test
  void testNotEquals() {
    SpecificationRule obj1 = new SpecificationRule();
    SpecificationRule obj2 = new SpecificationRule();

    obj1.setSpecificationRuleId(new SpecificationRuleId(UUID.randomUUID(), UUID.randomUUID()));
    obj2.setSpecificationRuleId(new SpecificationRuleId(UUID.randomUUID(), UUID.randomUUID()));

    assertFalse(obj1.equals(obj2) && obj2.equals(obj1));
  }

}
