package org.folio.rspec.domain.repository;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FieldRepository extends JpaRepository<Field, UUID> {

  @Query("select f from Field f where f.specification.id = ?1")
  List<Field> findBySpecificationId(UUID specificationId);

}
