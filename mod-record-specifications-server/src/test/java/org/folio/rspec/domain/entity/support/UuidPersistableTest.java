package org.folio.rspec.domain.entity.support;

import java.util.Objects;
import java.util.UUID;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class UuidPersistableTest {

  private UUID id;
  private TestEntity persistable;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    persistable = new TestEntity();
    persistable.setId(id);
  }

  @Test
  void testGetId() {
    Assertions.assertEquals(id, persistable.getId());
  }

  @Test
  void testIsNew() {
    persistable.setId(null);
    Assertions.assertTrue(persistable.isNew());
  }

  @Test
  void testHashCode() {
    Assertions.assertEquals(Objects.hashCode(id), persistable.hashCode());
  }

  @Test
  void testEquals() {
    TestEntity another = new TestEntity();
    another.setId(id);
    Assertions.assertEquals(persistable, another);
  }

  @Test
  void testNotEquals() {
    TestEntity another = new TestEntity();
    another.setId(UUID.randomUUID());
    Assertions.assertNotEquals(persistable, another);
  }

  @Test
  void testToString() {
    String expectedToString = String.format("Entity of type %s with id: %s", persistable.getClass().getName(), id);
    Assertions.assertEquals(expectedToString, persistable.toString());
  }

  private static final class TestEntity extends UuidPersistable { }
}
