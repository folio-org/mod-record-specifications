package org.folio.rspec.domain.repository;

import java.util.UUID;
import org.folio.rspec.domain.entity.SpecificationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpecificationMetadataRepository extends JpaRepository<SpecificationMetadata, UUID> {

  @Query("select s from SpecificationMetadata s where s.specification.id = ?1")
  SpecificationMetadata findBySpecificationId(UUID id);

}
