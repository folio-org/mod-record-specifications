package org.folio.rspec.domain.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.entity.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpecificationRepository extends JpaRepository<Specification, UUID>,
  JpaSpecificationExecutor<Specification> {

  default Page<Specification> findByFamilyAndProfile(Family family, FamilyProfile familyProfile,
                                                     Pageable pageable) {
    return findAll(familyAndProfileEq(family, familyProfile), pageable);
  }

  static org.springframework.data.jpa.domain.Specification<Specification> familyAndProfileEq(
    Family family, FamilyProfile familyProfile) {
    return (root, query, cb) -> {
      Predicate predicate = cb.conjunction();

      predicate = applyEqualityIfNotNull(predicate, cb, root.get(Specification.FAMILY_COLUMN), family);
      predicate = applyEqualityIfNotNull(predicate, cb, root.get(Specification.PROFILE_COLUMN), familyProfile);

      return predicate;
    };
  }

  private static <T> Predicate applyEqualityIfNotNull(Predicate predicate, CriteriaBuilder cb, Path<T> path, T value) {
    if (value != null) {
      return cb.and(predicate, cb.equal(path, value));
    }
    return predicate;
  }
}
