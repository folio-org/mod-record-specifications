package org.folio.rspec.domain.repository;

import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FieldRepository extends JpaRepository<Field, UUID> {

  @Query("select f from Field f where f.specification.id = ?1 order by f.tag")
  List<Field> findBySpecificationId(UUID specificationId);

  @Query("select f from Field f where f.specification.id = ?1 and f.required = ?2 order by f.tag")
  List<Field> findBySpecificationIdAndRequired(UUID specificationId, boolean required);

  @Transactional
  @Modifying
  @Query("delete from Field f where f.specification.id = ?1")
  void deleteBySpecificationId(UUID specificationId);
}
