package org.folio.rspec.domain.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.entity.Specification;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@EnablePostgres
@IntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpecificationRepositoryIT {

  @Autowired
  private SpecificationRepository specificationRepository;

  @Test
  void findByFamilyAndProfile() {
    Page<Specification> specifications = specificationRepository.findByFamilyAndProfile(
      Family.MARC, FamilyProfile.AUTHORITY, PageRequest.of(0, 10));

    assertEquals(1, specifications.getTotalElements());
  }

  @Test
  void findByFamily() {
    Page<Specification> specifications = specificationRepository.findByFamilyAndProfile(
      Family.MARC, null, PageRequest.of(0, 10));

    assertEquals(2, specifications.getTotalElements());
  }

  @Test
  void findByProfile() {
    Page<Specification> specifications = specificationRepository.findByFamilyAndProfile(
      null, FamilyProfile.AUTHORITY, PageRequest.of(0, 10));

    assertEquals(1, specifications.getTotalElements());
  }

}
