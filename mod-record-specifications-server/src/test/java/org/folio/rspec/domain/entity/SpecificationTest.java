package org.folio.rspec.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.entity.support.Metadata;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class SpecificationTest {

  private Specification specification;

  @BeforeEach
  void setUp() {
    specification = new Specification();
    specification.setId(UUID.randomUUID());
  }

  @Test
  void testGettersAndSetters() {
    UUID id = UUID.randomUUID();
    String title = "sample title";
    Family family = Family.MARC;
    FamilyProfile familyProfile = FamilyProfile.AUTHORITY;
    String url = "http://example.com";
    Metadata metadata = new Metadata();

    specification.setId(id);
    specification.setTitle(title);
    specification.setFamily(family);
    specification.setProfile(familyProfile);
    specification.setUrl(url);
    specification.setMetadata(metadata);

    assertEquals(id, specification.getId());
    assertEquals(title, specification.getTitle());
    assertEquals(family, specification.getFamily());
    assertEquals(familyProfile, specification.getProfile());
    assertEquals(url, specification.getUrl());
    assertEquals(metadata, specification.getMetadata());
  }

  @Test
  void testEqualsAndHashCode() {
    Specification spec2 = new Specification();
    spec2.setId(specification.getId());

    assertEquals(specification, spec2);
    assertEquals(specification.hashCode(), spec2.hashCode());
  }
}
